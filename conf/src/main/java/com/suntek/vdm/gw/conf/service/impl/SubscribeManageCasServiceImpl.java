package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import com.suntek.vdm.gw.conf.pojo.*;
import com.suntek.vdm.gw.conf.service.NotifyExcecutorService;
import com.suntek.vdm.gw.conf.service.SubscribeManageService;
import com.suntek.vdm.gw.conf.service.internal.InternalLinkService;
import com.suntek.vdm.gw.conf.ws.server.WsOperate;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.task.TaskManage;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.smc.service.SmcOtherService;
import com.suntek.vdm.gw.smc.service.SmcSubscribeService;
import com.suntek.vdm.gw.smc.ws.stomp.MyStompSession;
import com.suntek.vdm.gw.smc.ws.stomp.MySubscription;
import com.suntek.vdm.gw.welink.enums.UrlRegex;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 订阅内存管理 不处理实际业务
 */
@Service
@Slf4j
public class SubscribeManageCasServiceImpl implements SubscribeManageService {
    @Autowired
    private SmcSubscribeService smcSubscribeService;
    @Autowired
    private TaskManage taskManage;
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private  InternalLinkService internalLinkService;
    @Autowired
    private WsOperate wsOperate;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private SmcOtherService smcOtherService;
    @Autowired
    @Lazy
    private NotifyExcecutorService notifyExcecutorService;


    private static Map<String, SubscribeInfo> SUBSCRIBE_MAP;

    private static Map<String, List<SubscribeDestinationUserInfo>> DESTINATION_USER_MAP;

    public static Map<String, List<SubscribeDestinationUserInfo>> getDestinationUserMap() {
        if (DESTINATION_USER_MAP == null) {
            DESTINATION_USER_MAP = new ConcurrentHashMap<>();
        }
        return DESTINATION_USER_MAP;
    }


    public Map<String, SubscribeInfo> getSubscribeMap() {
        if (SUBSCRIBE_MAP == null) {
            SUBSCRIBE_MAP = new ConcurrentHashMap<>();
        }
        return SUBSCRIBE_MAP;
    }

    public  Queue<ReSubscribeInfo> getReSubscribeInfoQueue() {
        if (reSubscribeInfoQueue==null){
            reSubscribeInfoQueue=new LinkedList<>();
        }
        return reSubscribeInfoQueue;
    }

    private static Queue<ReSubscribeInfo> reSubscribeInfoQueue;


    @Override
    public SubscribeInfo getSubscribeInfo(String sessionId) {
        return getSubscribeMap().get(sessionId);
    }

    @Override
    public List<SubscribeDestinationUserInfo> getDestinationUser(String destination) {
        return getDestinationUserMap().get(destination);
    }

    @Override
    public boolean connect(String sessionId, String user, SubscribeUserType type, String tickets, String token, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        connectBefore(sessionId);
        LocalToken localToken = localTokenManageService.get(token);
        String smcSessionId = smcSubscribeService.connect(localToken.getUsername(), tickets, localToken.getSmcToken(), stompSessionHandlerAdapter);
        connectAfter(sessionId, smcSessionId, user,token, type);
        return smcSessionId!=null;
    }

    @Override
    public boolean connect(String sessionId, String user, SubscribeUserType type, String authorization, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        connectBefore(sessionId);
        String smcSessionId = smcSubscribeService.connect(authorization, stompSessionHandlerAdapter);
        connectAfter(sessionId, smcSessionId, user,authorization, type);
        return smcSessionId!=null;
    }

    @Override
    public void disconnect(String sessionId) {
        smcSubscribeService.disconnect(getSmcSessionId(sessionId));
        SubscribeInfo subscribeInfo = getSubscribeMap().remove(sessionId);
        if (subscribeInfo != null) {
            Map<String, SubscribeDetail> stringSubscribeDetailMap = subscribeInfo.getSubscribeDetailMap();
            for (Map.Entry<String, SubscribeDetail> item : stringSubscribeDetailMap.entrySet()) {
                String destination = item.getValue().getDestination();
                if (getDestinationUserMap().containsKey(destination)) {
                    getDestinationUserMap().get(destination).removeIf(x -> x.getUser().equals(subscribeInfo.getUser()));
                    if (getDestinationUserMap().get(destination).size() == 0) {
                        //没有订阅了删除列表
                        getDestinationUserMap().remove(destination);
                    }
                }
            }
            wsOperate.closeUser(sessionId);
        }
    }


    @Override
    public void subscribe(String sessionId, @Nullable String subId, String destination, String backDestination, StompFrameHandler handler, StompHeaders headers) {
        //订阅Id为空默认和路径保持一致
        if (subId == null) {
            subId = destination;
        }
        //添加 路径 用户
        if (!getDestinationUserMap().containsKey(destination)) {
            getDestinationUserMap().put(destination, new ArrayList<>());
        }
        List<SubscribeDestinationUserInfo> subscribeDestinationUserInfos = getDestinationUserMap().get(destination);
        SubscribeInfo subscribeInfo = getSubscribeMap().get(sessionId);

        //判断本级是否订阅该路径是否已经订阅
        checkLocalSubscribe(destination, subscribeDestinationUserInfos, subscribeInfo);

        smcSubscribeService.subscribe(getSmcSessionId(sessionId), destination, subId, handler, headers);
        getSubscribeInfo(sessionId).addSubscribe(subId, destination);
        //FIXME 用户重复性判断 目前不判断 用户操作优先
        if (subscribeInfo.getType().equals(SubscribeUserType.REMOTE)){
            if (subscribeDestinationUserInfos.stream().filter(x -> x.getUser().equals(subscribeInfo.getUser()) && x.getDestination().equals(destination)).findFirst().isPresent()) {
                return;
            }
        }
        subscribeDestinationUserInfos.add(new SubscribeDestinationUserInfo(subscribeInfo.getUser(), destination, backDestination, subId, subscribeInfo.getType()));
    }

    private void checkLocalSubscribe(String destination, List<SubscribeDestinationUserInfo> subscribeDestinationUserInfos, SubscribeInfo subscribeInfo) {
        if(!SystemConfiguration.smcVersionIsV2() || subscribeDestinationUserInfos == null || "/topic/conferences/status".equals(destination) || CoreConfig.INTERNAL_SUBSCRIBE_USER.equals(subscribeInfo.getUser())){
            return;
        }
        boolean isHasLocalSubscribe = false;
        for (SubscribeDestinationUserInfo subscribeDestinationUserInfo: subscribeDestinationUserInfos){
            if (CoreConfig.INTERNAL_SUBSCRIBE_USER.equals(subscribeDestinationUserInfo.getUser())){
                isHasLocalSubscribe = true;
                break;
            }
        }
        if (!isHasLocalSubscribe){
            ConferenceStatusNotify conferenceStatusNotify = new ConferenceStatusNotify();
            ConferenceStatusInfo conferenceStatusInfo = new ConferenceStatusInfo();
            String conferenceId = getConferenceIdByDestination(destination);
            log.info("check local subscribe:destination:{} , subscribeDestinationUserInfos:{},conferenceId:{}", destination, subscribeDestinationUserInfos, conferenceId);
            conferenceStatusInfo.setConferenceId(conferenceId);
            conferenceStatusInfo.setStage(CoreConfig.CONFERENCE_ONLINE);
            conferenceStatusNotify.setConferenceStages(Collections.singletonList(conferenceStatusInfo));
            notifyExcecutorService.dealConferenceStatusNotify(conferenceStatusNotify);
        }
    }

    private String getConferenceIdByDestination(String destination) {
        String regex = "/topic/conferences/([^/ ]+)/participants/general";
        if (destination.matches(regex)) {
            //查询会议详情
            return getPathVar(regex, destination, 1)[0];
        }
        regex = "/topic/conferences/([^/ ]+)";
        if (destination.matches(regex)) {
            //查询会议详情
            return getPathVar(regex, destination, 1)[0];
        }
        return null;
    }
    /**
     *
     * @param regex 正则表达式
     * @param url   url
     * @param paramCount url中参数的个数
     * @return 参数数组，从前往后排序
     */
    public String[] getPathVar(String regex, String url,int paramCount) {
        String[] params = new String[paramCount];
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(url);
        if (matcher.find()) {
            for (int i = 1; i <= paramCount; i++) {
                params[i - 1] = matcher.group(i);
            }
            return params;
        }
        return null;
    }

    @Override
    public void unSubscribe(String sessionId, String subId) {
        //删除 路径 用户
        String destination = getDestination(sessionId, subId);
        String user = getUser(sessionId);
        if (destination != null && getDestinationUserMap().containsKey(destination)) {
            getDestinationUserMap().get(destination).removeIf(x -> x.getUser().equals(user) && x.getSubId().equals(subId));
        }
        if (destination != null && getDestinationUserMap().get(destination).size() == 0) {
            //没有订阅了删除列表
            getDestinationUserMap().remove(destination);
        }
        smcSubscribeService.unSubscribe(getSmcSessionId(sessionId), subId);
        SubscribeInfo subscribeInfo = getSubscribeInfo(sessionId);
        if(subscribeInfo == null){
            return;
        }
        subscribeInfo.delSubscribe(subId);
    }

    @Override
    public void modifyKey(String oldKey, String newKey) {
        getSubscribeMap().put(newKey, getSubscribeMap().remove(oldKey));
    }

    @Override
    public boolean isOpen(String sessionId) {
        if (!getSubscribeMap().containsKey(sessionId)) {
            return false;
        }
        return smcSubscribeService.isOpen(getSmcSessionId(sessionId));
    }

    @Override
    public String getDestination(String sessionId, String subId) {
        SubscribeInfo subscribeInfo = getSubscribeInfo(sessionId);
        if(subscribeInfo == null){
            return null;
        }
        SubscribeDetail subscribeDetail = subscribeInfo.getSubscribeDetailMap().get(subId);
        if (subscribeDetail == null){
            return null;
        }
        return subscribeDetail.getDestination();
    }

    public String getUser(String sessionId) {
        SubscribeInfo subscribeInfo = getSubscribeMap().get(sessionId);
        if(subscribeInfo == null){
            return null;
        }
        return subscribeInfo.getUser();
    }


    @Override
    public boolean hasSubScribe(String sessionId, String destination) {
        //订阅用的是destination作为subId,所以可以直接置换
        String subId = destination;
        try {
            return getSubscribeInfo(sessionId).getSubscribeDetailMap().containsKey(subId);
        } catch (Exception e) {
            log.error("exception",e);
            return false;
        }
    }

    @Override
    public boolean hasSubScribe(String destination) {
        if (getDestinationUserMap().containsKey(destination)) {
            return getDestinationUserMap().get(destination).size() > 0;
        } else {
            return false;
        }
    }

    private String getSmcSessionId(String sessionId) {
        if (sessionId==null){
            return null;
        }
        SubscribeInfo subscribeInfo=getSubscribeInfo(sessionId);
        if (subscribeInfo==null){
            return null;
        }else{
            return subscribeInfo.getSmcSessionId();
        }
    }

    private String getSessionIdBySmcSessionId(String smcSessionId) {
        for (Map.Entry<String, SubscribeInfo> item : getSubscribeMap().entrySet()) {
            if (item.getValue().getSmcSessionId().equals(smcSessionId)) {
                return item.getKey();
            }
        }
        return null;
    }

    public boolean reconnect(String smcSessionId, String username, String tickets, String smcToken, MyStompSession myStompSession, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        String sessionId = getSessionIdBySmcSessionId(smcSessionId);
        if (sessionId != null) {
            String newSmcSessionId = smcSubscribeService.connect(username, tickets, smcToken, stompSessionHandlerAdapter);
            if (newSmcSessionId==null){
                return false;
            }
            reconnectAfter(sessionId, newSmcSessionId, myStompSession);
        }
        return true;
    }

    public boolean reconnect(String smcSessionId, String authorization, MyStompSession myStompSession, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        String sessionId = getSessionIdBySmcSessionId(smcSessionId);
        if (sessionId != null) {
            String newSmcSessionId = smcSubscribeService.connect(authorization, stompSessionHandlerAdapter);
            if (newSmcSessionId==null){
                return false;
            }
            reconnectAfter(sessionId, newSmcSessionId, myStompSession);
        }
        return true;
    }

    public void reSubscribe(String smcSessionId, String subId, String destination, StompFrameHandler handler, StompHeaders headers) {
        log.info("Re subscribe by smcSessionId:{},destination:{},sunId:{}", smcSessionId, destination, subId);
        smcSubscribeService.subscribe(smcSessionId, destination, subId, handler, headers);
    }

    private void connectBefore(String sessionId) {
        if (isOpen(sessionId)) {
            disconnect(sessionId);
        }
    }

    private void connectAfter(String sessionId, String smcSessionId, String user, String token, SubscribeUserType type) {
        if (smcSessionId==null){
            return;
        }
        SubscribeInfo subscribeInfo = new SubscribeInfo(sessionId, smcSessionId, user,token, type);
        getSubscribeMap().put(sessionId, subscribeInfo);
        taskManage.stompCheckStart();
    }

    private void reconnectAfter(String sessionId, String newSmcSessionId, MyStompSession myStompSession) {
        //替换新连接的SMC sessionId
        getSubscribeInfo(sessionId).setSmcSessionId(newSmcSessionId);
        for (MySubscription item : myStompSession.getSubscriptionMap().values()) {
            StompSession.Subscription subscription = item.getSubscription();
            String destination = subscription.getSubscriptionHeaders().getDestination();
            //延迟防止 SMC 流控
            CommonHelper.sleep(200);
            reSubscribe(newSmcSessionId, destination, subscription.getSubscriptionId(), item.getHandler(), subscription.getSubscriptionHeaders());
        }
    }

    public void reconnect(MyStompSession myStompSession, StompSessionHandlerAdapter stompSessionHandlerAdapter) {
        String smcSessionId = myStompSession.getStompSession().getSessionId();
        if (smcSessionId==null){
            log.info("reconnect smc SessionId is null");
            return;
        }
        String sessionId = getSessionIdBySmcSessionId(smcSessionId);
        if (sessionId==null){
            log.info("reconnect smc sessionId is null");
            return;
        }
        boolean flag=true;
        SubscribeUserType type = getSubscribeMap().get(sessionId).getType();
        log.info("reconnect by subscribe user type:{}", type.toString());
        switch (type) {
            case REMOTE: {
                SubscribeInfo subscribeInfo = getSubscribeMap().get(sessionId);
                LocalToken localToken = localTokenManageService.getByNodeId(sessionId);
                try {
                    if (localToken == null) {
                        return;
                    }
                    String tickets = smcOtherService.getMeetingTickets(localToken.getUsername(), localToken.getSmcToken());
                    flag=reconnect(sessionId, subscribeInfo.getUser(), tickets, localToken.getSmcToken(), myStompSession, stompSessionHandlerAdapter);
                } catch (MyHttpException e) {
                    log.error("Get Meeting Tickets fail,error：{}", e.toString());
                }
                break;
            }
            case LOCAL: {
                wsOperate.closeUser(sessionId);//向客户端发送错误 客户端自己发起关闭
                break;
            }
            case INTERNAL: {
                internalLinkService.start();
                break;
            }
            default:
                break;
        }
        if (!flag){
            ReSubscribeInfo reSubscribeInfo=new ReSubscribeInfo(myStompSession,stompSessionHandlerAdapter);
            getReSubscribeInfoQueue().offer(reSubscribeInfo);
        }
    }
}

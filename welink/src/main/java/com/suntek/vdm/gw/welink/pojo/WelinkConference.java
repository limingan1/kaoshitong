package com.suntek.vdm.gw.welink.pojo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.neovisionaries.ws.client.WebSocket;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.CascadeChannelNotifyType;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.common.pojo.websocket.CascadeChannelMessage;
import com.suntek.vdm.gw.common.pojo.websocket.MessageContent;
import com.suntek.vdm.gw.common.pojo.websocket.MessageType;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;
import com.suntek.vdm.gw.common.service.SpringUtil;
import com.suntek.vdm.gw.common.util.TransactionManage;
import com.suntek.vdm.gw.welink.api.enumeration.Status;
import com.suntek.vdm.gw.welink.api.pojo.InviteResultDto;
import com.suntek.vdm.gw.welink.api.pojo.ParticipantInfo;
import com.suntek.vdm.gw.welink.api.request.LockViewParticipantsRequest;
import com.suntek.vdm.gw.welink.api.request.PartViewParticipantsRequest;
import com.suntek.vdm.gw.welink.api.service.WeLinkMeetingControlService;
import com.suntek.vdm.gw.welink.service.impl.WelinkUtilService;
import com.suntek.vdm.gw.welink.util.CasChannelPartyUtil;
import com.suntek.vdm.gw.welink.websocket.WeLinkWebSocketService;
import com.suntek.vdm.gw.welink.websocket.WelinkWebSocketClient;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Data
@Slf4j
public class WelinkConference {
    private String id;
    private String accessCode;
    private String smcAccessCode;
    private String name;
    private WebSocket webSocket;
    private SubscribeMessage conferenceSubscribeMessage;
    private SubscribeMessage subscribeMessage;
    private Integer maxCascadeNum;
    private GwId remoteGwId;
    /**
     * 全部会场
     */
    private Map<String, ParticipantInfo> allParticipantMap = new ConcurrentHashMap<>();

    /**
     * 级联通道状态
     */
    private Map<String, CascadeChannelInfo> cascadeChannelInfoMap = new ConcurrentHashMap<>();
    
    private String wsURL;
    private String conferenceToken;
    private boolean isAttendFullReceive = false;
    private boolean isAllParticipantReceive = false;
    private Queue<JSONObject> queueCache = new LinkedList<>();
    private Map<String, String> URI_TO_PID_MAP = new HashMap<>();
    public Map<String, String> PID_TO_URI_MAP = new HashMap<>();
    //当前会议在线会场
    public Map<String, Boolean> ONLINE_URI_MAP = new HashMap<>();

    private int confTokenExpireTime = 0;
    private int tokenStatus = 0;
    private String strPresentationUri = null;
    //广播
    private String broadcastUri;
    private String mainChannelSource;
    //辅流
    private String presenterId;
    private String spokesmanId;
    private WeLinkWebSocketService weLinkWebSocketService;
    private String confMode = "COMMON";

    public void setConfMode(String confMode) {
        this.confMode = confMode;
    }

    //保证在发往welink的请求时，token不为空
    public synchronized void assertTokenNotNull(String token) {
        if (token == null) {
            //assert
            if (conferenceToken == null || conferenceToken.equals("")) {
                TransactionManage.wait(new TransactionId(TransactionType.WELINK_TOKEN, id),3000);
                assertTokenNotNull(null);
            }
        }else{
            //set
            conferenceToken = token;
            TransactionManage.notify(new TransactionId(TransactionType.WELINK_TOKEN, id));
//            return conferenceToken;
        }
    }

    public WelinkConference(String id, String accessCode,String name1,String smcAccessCode,int maxCascadeNum, WeLinkWebSocketService weLinkWebSocketService) {
        this.id = id;
        this.smcAccessCode = smcAccessCode;
        this.accessCode = accessCode;
        this.name = name1;
        this.maxCascadeNum = maxCascadeNum;
        this.weLinkWebSocketService = weLinkWebSocketService;
    }

    public void refreshToken(JSONObject refreshData) {
        JSONObject data = refreshData.getJSONObject("data");
        if (data != null) {
            assertTokenNotNull(data.getString("token"));
            wsURL = data.getString("wsURL");
            confTokenExpireTime = data.getIntValue("confTokenExpireTime");
        }
    }

    public void setConferenceSubscribeMessage(SubscribeMessage conferenceSubscribeMessage) {
        boolean needSendAllNotify = false;
        if(this.conferenceSubscribeMessage == null){
            needSendAllNotify = true;
        }
        this.conferenceSubscribeMessage = conferenceSubscribeMessage;
        if(isAllParticipantReceive && needSendAllNotify){
            sendAllMessage(id);
        }
    }


    private int getCurrentOnlineNum(){
        int onlineNum = 0;
        for(Boolean status: ONLINE_URI_MAP.values()){
            if(status){
                onlineNum++;
            }
        }
        return onlineNum;
    }

    public ParticipantInfo getParticipantInfo(String uri){
        return allParticipantMap.get(uri);
    }


    private void clearMemory(){
        log.info("clear old memory.");
        URI_TO_PID_MAP.clear();
        PID_TO_URI_MAP.clear();
        allParticipantMap.clear();
        cascadeChannelInfoMap.clear();
    }

    public void dealAttendAllMessage(JSONArray attends) {
        clearMemory();
        log.info("wss [dealAttendAllMessage] attends->" + attends);
        dealAttendJson(attends, true);
        dealQueueCache();
    }
    private void dealQueueCache() {
        isAttendFullReceive = true;
        int queueSize = queueCache.size();
        if (queueSize <= 0) {
            return;
        }
        for (int i = 0; i < queueSize; i++) {
            JSONObject jsonObject = queueCache.poll();
            assert jsonObject != null;
            WelinkUtilService.dealWebsocketJson(jsonObject,this);
        }
    }

    private void dealAttendJson(JSONArray attends, boolean b) {
        for (int i = 0; i < attends.size(); i++) {
            JSONObject attend = attends.getJSONObject(i);
            String uri = attend.getString("phone");
            ParticipantInfo site = allParticipantMap.get(uri);
            if (site == null) {
                attend.put("isNew", true);
                site = new ParticipantInfo();
            }
            site.setAttend(true);
            allParticipantMap.put(uri, site);
            doParser(attend, true, b);
        }
    }

    private void doParser(JSONObject attend, boolean isAttend, boolean isAllMessage) {
        if (isAttend) {
            dealAttendeesJson(attend, isAllMessage);
            return;
        }
        //处理participant消息
        Map<String, String> participantsInfoMap = new HashMap<>(16);

        String pid = attend.getString("pid");
        JSONObject participants = attend.getJSONObject("pinfoMap");
        String uri = participants.getString("TEL");
        if (uri == null) {
            uri = PID_TO_URI_MAP.get(pid);
        }
        String participantType = setWelinkUpSiteType(uri);
        participantsInfoMap.put("TYPE", participantType);
        participantsInfoMap.put("URI", uri);
        participantsInfoMap.put("PID", pid);
        ParticipantInfo participantSite = allParticipantMap.get(uri);
        //初始其他几个参数
        initParticipantsMap(participantsInfoMap, participants);
        dealBroadcastAndSpokesman(participantsInfoMap);
        if (StringUtils.isEmpty(participantSite.getPhone())) {
            dealUninvited(participantSite, participantsInfoMap);
            return;
        }
        if (isAllMessage) {
            dealParticipantAllMess(participantSite, participantsInfoMap);
            return;
        }
        dealParticipantChangeMess(participantSite, participantsInfoMap);
    }

    private void dealBroadcastAndSpokesman(Map<String, String> participantsInfoMap) {
        String broadcastValue = participantsInfoMap.get("BROADCAST");
        String SpokesmanValue = participantsInfoMap.get("ROLLCALL");
        String uri = participantsInfoMap.get("URI");
        if(!StringUtils.isEmpty(broadcastValue) || !StringUtils.isEmpty(SpokesmanValue)){
            if((!"0".equals(broadcastValue) || !"0".equals(SpokesmanValue)) && !uri.equals(broadcastUri)){
                broadcastUri = uri;
                sendUpchannelSourceChange(uri);
                if(!isUploadToSmc(smcAccessCode,uri)){
                    sendAllParticipantsBroadcast(uri, null);
                }else{
                    sendAllParticipantsBroadcast(mainChannelSource, null);
                }
                //发出会议广播通知
                sendConferenceNotify(true);
            }
        }
    }

    public void sendUpchannelSourceChange(String uri){
        if (!isUploadToSmc(smcAccessCode,uri)) {
            //通知主通道视频源改变
            MultiPicInfo multiPicInfo = new MultiPicInfo();
            multiPicInfo.setMode(1);
            multiPicInfo.setPicNum(1);
            SubPic subPic = new SubPic();
            subPic.setParticipantId(uri);
            ParticipantInfo participantInfo = getParticipantInfo(uri);
            subPic.setName(participantInfo.getName());
            multiPicInfo.setSubPicList(Collections.singletonList(subPic));
            notifyUp(multiPicInfo, id, smcAccessCode, 0);
        }
    }

    public void notifyUp(MultiPicInfo multiPicInfo, String conferenceId, String accessCode, Integer channelIndex){
        CascadeChannelNotifyInfo cascadeChannelNotifyInfo = new CascadeChannelNotifyInfo();
        cascadeChannelNotifyInfo.setIndex(channelIndex);
        //设置反方向
        cascadeChannelNotifyInfo.setDirection(CascadeParticipantDirection.DOWN);
        cascadeChannelNotifyInfo.setMultiPicInfo(multiPicInfo);
        cascadeChannelNotifyInfo.setConfCasId(accessCode);
        //本级的会议号  用于远端查找
        cascadeChannelNotifyInfo.setRemoteConferenceId(conferenceId);
        cascadeChannelNotifyInfo.setCascadeChannelNotifyType(CascadeChannelNotifyType.VIDEO_SOURCE);
        MessageContent message = new MessageContent(MessageType.NOTIFY_SOURCE, JSON.toJSONString(cascadeChannelNotifyInfo));
        sendNotify(JSON.toJSONString(message));
    }


    public void sendConferenceNotify(Boolean isSendMasterChannelNotify) {
        if(subscribeMessage == null){
            log.warn("subscribeMessage is empty.");
            return;
        }
        ConferencesControllerStatusNotify conferencesControllerStatusNotify = new ConferencesControllerStatusNotify();
        ConferenceParam conferenceParam = new ConferenceParam();
        conferenceParam.setConferenceId(id);
        ConferenceState conferenceState = new ConferenceState();
        conferenceState.setConferenceId(id);
        conferenceState.setBroadcastId(broadcastUri);
        conferenceState.setPresenterId(presenterId);
        conferenceState.setCurrentSpokesmanId(spokesmanId);
        conferencesControllerStatusNotify.setParam(conferenceParam);
        conferencesControllerStatusNotify.setState(conferenceState);
        conferencesControllerStatusNotify.setConfCasId(accessCode);
        subscribeMessage.setMessage(JSON.toJSONString(conferencesControllerStatusNotify));
        MessageContent messageContent = new MessageContent();
        messageContent.setBody(JSON.toJSONString(subscribeMessage));
        messageContent.setType(MessageType.SUBSCRIBE_MESSAGE);
        log.info("ws send message:{}", messageContent);
        WebSocket webSocket1 = WelinkWebSocketClient.getWebSocket();
        if (webSocket1 == null) {
            synchronized (this) {
                //连接conf websocket
                webSocket1 = WelinkWebSocketClient.getWebSocket();
                if (webSocket1 == null) {
                    webSocket1 = WelinkWebSocketClient.openNodeWebsocket();
                }
            }
            log.info("ws connect success");
        }
        webSocket1.sendText(JSON.toJSONString(messageContent));
        log.info("ws send message success");

        //获取主通道
        if(isSendMasterChannelNotify) {
            sendMasterVideoSourceNotify(broadcastUri);
        }

    }

    public void sendMasterVideoSourceNotify(String uri) {
        log.info("master channel source: {}", uri);
        ParticipantInfo participantInfo = getMasterParticipant();
        if (participantInfo != null) {
            if(participantInfo.getUri() != null && !participantInfo.getUri().equals(uri)){
                participantInfo.setMultiPicInfo(uri);
            }
            sendNotify(Collections.singletonList(participantInfo.toParticipantStatusInfo(id)), 3, id, conferenceSubscribeMessage);
        }
    }

    public ParticipantInfo getMasterParticipant(){
        for(CascadeChannelInfo cascadeChannelInfo: cascadeChannelInfoMap.values()){
            if(cascadeChannelInfo.getIndex() != 0){
                continue;
            }
            return allParticipantMap.get(cascadeChannelInfo.getParticipantId());
        }
        return null;
    }

    private void dealParticipantAllMess(ParticipantInfo participantSite, Map<String, String> participantsInfoMap) {
        String uri = participantsInfoMap.get("URI");
        String pid = participantsInfoMap.get("PID");
        dealSiteParticipant(participantSite, participantsInfoMap);
        participantSite.setParticipantID(uri);
        participantSite.setPId(pid);
        participantSite.setUri(uri);
        allParticipantMap.put(uri, participantSite);
    }
    private void dealParticipantChangeMess(ParticipantInfo participantSite, Map<String, String> participantsInfoMap) {
        String uri = participantsInfoMap.get("URI");
        boolean hasChange;
        if("0".equalsIgnoreCase(participantsInfoMap.get("STATE")) && !"0".equals(participantSite.getState())){
            //当前广播态为主通道
            if(!StringUtils.isEmpty(broadcastUri) && isUploadToSmcMaster(smcAccessCode, broadcastUri)){
                participantSite.setMultiPicInfo(mainChannelSource);
            }
        }
        hasChange = dealSiteParticipant(participantSite, participantsInfoMap);
        if (hasChange) {
            participantSite.setStatus(Status.MOD_ON_CHANGE);
        }
        allParticipantMap.put(uri, participantSite);
    }
    private void initParticipantsMap(Map<String, String> participantsInfoMap, JSONObject participants) {
        String name1 = participants.getString("NAME");
        String role = participants.getString("ROLE");
        String state = participants.getString("STATE");
        String broadcast = participants.getString("BROADCAST");
        String rollCall = participants.getString("ROLLCALL");
        String mute = participants.getString("MUTE");
        String video = participants.getString("VIDEO");
        String mNumber = participants.getString("M");
        String share = participants.getString("SHARE");
        String lockView = participants.getString("LOCKED_VIEW");
        if (StringUtils.isNotEmpty(name1)) {
            participantsInfoMap.put("NAME", name1);
        }
        if (StringUtils.isNotEmpty(role)) {
            participantsInfoMap.put("ROLE", role);
        }
        if (StringUtils.isNotEmpty(state)) {
            participantsInfoMap.put("STATE", state);
        }
        if (StringUtils.isNotEmpty(broadcast)) {
            participantsInfoMap.put("BROADCAST", broadcast);
        }
        if (StringUtils.isNotEmpty(rollCall)) {
            participantsInfoMap.put("ROLLCALL", rollCall);
        }
        if (StringUtils.isNotEmpty(mute)) {
            participantsInfoMap.put("MUTE", mute);
        }
        if (StringUtils.isNotEmpty(video)) {
            participantsInfoMap.put("VIDEO", video);
        }
        if (StringUtils.isNotEmpty(mNumber)) {
            participantsInfoMap.put("M", mNumber);
        }
        if (StringUtils.isNotEmpty(share)) {
            participantsInfoMap.put("SHARE", share);
        }
        if (StringUtils.isNotEmpty(lockView)){
            participantsInfoMap.put("LOCKED_VIEW", lockView);
        }
    }

    private String setWelinkUpSiteType(String uri) {
        String participantType = "Site";
        //设置max
        if (isUploadToSmc(smcAccessCode, uri)) {
            participantType = "UpperLevelParticipant";
            Integer maxCascadeNum;
            if (isUploadToSmcMaster(smcAccessCode, uri)) {
                maxCascadeNum = getCascadeCountFormUri(uri, smcAccessCode);
                setCasChannelMaxNumber(maxCascadeNum);
            }
        }
        return participantType;
    }
    //处理不请自来
    private void dealUninvited(ParticipantInfo participantSite, Map<String, String> participantsInfoMap) {
        boolean hasChange;
        hasChange = dealSiteParticipant(participantSite, participantsInfoMap);
        String uri = participantsInfoMap.get("URI");
        String pid = participantsInfoMap.get("PID");
        participantSite.setParticipantID(uri);
        participantSite.setPId(pid);
        if (hasChange) {
            if(isUploadToSmc(smcAccessCode,uri)){
                participantSite.setStatus(Status.ADD);
            }else{
                sendNotify(Collections.singletonList(participantSite.toParticipantStatusInfo(id)),1,id, conferenceSubscribeMessage);
                participantSite.setStatus(Status.MOD_ON_CHANGE);
            }
        }
        allParticipantMap.put(uri, participantSite);
    }
    private boolean dealSiteParticipant(ParticipantInfo participantSite, Map<String, String> participantsInfoMap) {
        //遍历map，对应值赋值。
        boolean change = false;
//        优先处理uri
        String uri = participantsInfoMap.get("URI");
        if(uri != null){
            String oldPhone = participantSite.getPhone();
            if (!uri.equals(oldPhone)) {
                participantSite.setPhone(uri);
                participantSite.setUri(uri);
                change = true;
            }
        }

        for (Map.Entry<String, String> entry : participantsInfoMap.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            switch (mapKey) {
                case "NAME":
                    String oldName = participantSite.getName();
                    if (!mapValue.equals(oldName)) {
                        participantSite.setName(mapValue);
                        change = true;
                    }
                    break;
                case "STATE":
                    String oldState = participantSite.getState();
                    if (!mapValue.equals(oldState) ) {
                        participantSite.setState(mapValue);
                        if (!isUploadToSmc(smcAccessCode, participantSite.getUri()) && "0".equals(mapValue)) {
                            ONLINE_URI_MAP.put(participantSite.getUri(), true);
                        }
                        change = true;
                    }
                    break;
                case "TYPE":
                    String oldType = participantSite.getAttendeeType();
                    if (!mapValue.equals(oldType)) {
                        participantSite.setAttendeeType(mapValue);
                        change = true;
                    }
                    break;
                case "ROLE":
                    Integer oldRole = participantSite.getRole();
                    if (mapValue != null) {
                        int role = Integer.parseInt(mapValue);
                        if(oldRole == null || role != oldRole){
                            participantSite.setRole("1".equals(mapValue) ? 1 : 0);
                            change = true;
                        }
                    }
                    break;
                case "VIDEO":
                    boolean bVideo = !"0".equals(mapValue);
                    Boolean oldVideo = participantSite.getVideo();
                    oldVideo = oldVideo != null && oldVideo;
                    if (bVideo != oldVideo) {
                        participantSite.setVideo(bVideo);
                        change = true;
                    }
                    break;
                case "MUTE":
                    boolean bMute = !"0".equals(mapValue);
                    Boolean oldMute = participantSite.getMute();
                    oldMute = oldMute != null && oldMute;
                    participantSite.setMute(bMute);
                    if (bMute != oldMute) {
                        change = true;
                    }
                    break;
                case "LOCKED_VIEW":
                    boolean lockView = !"0".equals(mapValue);
                    Boolean oldLockView = participantSite.getLockView();
                    oldLockView = oldLockView != null && oldLockView;
                    if (lockView != oldLockView) {
                        participantSite.setLockView(lockView);
                        change = true;
                    }
                    break;
                case "M":
                    if (StringUtils.isEmpty(mapValue)) {
                        break;
                    }
                    try {
                        int iMtNumber = Integer.parseInt(mapValue);
                        if (iMtNumber < 1) {
                            break;
                        }
                        Integer oldMtNumber = participantSite.getMtNumber();
                        if (oldMtNumber == null || iMtNumber != oldMtNumber) {
                            participantSite.setMtNumber(iMtNumber);
                            change = true;
                        }
                        break;
                    } catch (NumberFormatException e) {
                        log.error("mtNumber Integer exception" + mapValue);
                        continue;
                    }
                case "SHARE":
                    boolean bShare = !"0".equals(mapValue);
                    Boolean oldShare = participantSite.getShare();
                    oldShare = oldShare != null && oldShare;
                    participantSite.setShare(bShare);
                    if (bShare != oldShare) {
                        participantSite.setShareChange(true);
                        change = true;
                    } else {
                        if(participantSite.getShareChange() != null && !participantSite.getShareChange()){
                            participantSite.setShare(null);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return change;
    }

    private void dealAttendeesJson(JSONObject attend, boolean isAllMessage) {
        String isLeaveSite = "1";
        String accountId = attend.getString("accountId");
        String name1 = attend.getString("name");
        String phone = attend.getString("phone");
        String mode = attend.getString("mode");
        if (accountId == null) {
            if (!phone.isEmpty()) {
                accountId = phone;
            }
        }
        if (StringUtils.isNotEmpty(phone)) {
            ParticipantInfo attendeeSite = allParticipantMap.get(phone);
            //处理预约离线
            if (isLeaveSite.equals(mode)) {
                attendeeSite.setStatus(Status.DEL);
                attendeeSite.setShare(false);
                if(attendeeSite.getUri().equals(presenterId)){
                    presenterId = "";
                    sendConferenceNotify(true);
                }
                if(attendeeSite.getUri() != null && attendeeSite.getUri().equals(broadcastUri)){
                    broadcastUri = null;
                }
                allParticipantMap.put(phone, attendeeSite);
                return;
            }
            if (StringUtils.isNotEmpty(name1)) {
                attendeeSite.setName(name1);
            }
            attendeeSite.setUri(phone);
            attendeeSite.setPhone(phone);
            attendeeSite.setAccountId(accountId);
            if (!isAllMessage) {
                Boolean isNew = attend.getBooleanValue("isNew");
                if(isNew){
                    attendeeSite.setStatus(Status.ADD);
                }else{
                    attendeeSite.setStatus(Status.MOD_ON_CHANGE);
                }
            }
            attendeeSite.setAttendeeType("SIP");
            allParticipantMap.put(phone, attendeeSite);
            createCasChannel(attendeeSite.getUri());
        }
    }


    public void dealAttendIncreaseMessage(JSONArray attend,String conferenceId) {
        log.info("wss [dealAttendIncreaseMessage] attends->" + attend);
        dealAttendJson(attend, false);
        dealAllParticipantMap(conferenceId);
    }
    public synchronized void sendAllMessage(String conferenceId) {
        isAllParticipantReceive = true;
        if(conferenceSubscribeMessage == null){
            return;
        }
        List<ParticipantStatusInfo> changeList = new ArrayList<>();
        allParticipantMap.forEach((key,value)-> changeList.add(value.toParticipantStatusInfo(conferenceId)));

        sendNotify(changeList, 0, conferenceId, conferenceSubscribeMessage);
        sendNotify(changeList, 1, conferenceId, conferenceSubscribeMessage);
    }
    private void dealAllParticipantMap(String conferenceId) {
        allParticipantMap.forEach((key,participantInfo)->{
            switch (participantInfo.getStatus()) {
                case DEL:
                    sendNotify(Collections.singletonList(participantInfo.toParticipantStatusInfo(conferenceId)),2,conferenceId, conferenceSubscribeMessage);
                    allParticipantMap.remove(key);
                    break;
                case MOD_ON_CHANGE:
                    sendNotify(Collections.singletonList(participantInfo.toParticipantStatusInfo(conferenceId)),3,conferenceId, conferenceSubscribeMessage);
                    participantInfo.setStatus(Status.ON_NONE);
                    createCasChannel(participantInfo.getUri());
                    break;
                case ADD:
                    participantInfo.setStatus(Status.ON_NONE);
                    createCasChannel(participantInfo.getUri());
                    sendNotify(Collections.singletonList(participantInfo.toParticipantStatusInfo(conferenceId)),1,conferenceId, conferenceSubscribeMessage);
                    break;
                case ON_NONE:
                default:
                    break;
            }
        });
    }

    public void dealParticipantAllMessage(JSONObject jsonObject) {
        if (checkAttendReceived(jsonObject)) {
            return;
        }
        JSONArray participants = jsonObject.getJSONArray("data");
        log.info("wss [dealParticipantAllMessage] participants->" + participants);
        for (int i = 0; i < participants.size(); i++) {
            JSONObject participant = participants.getJSONObject(i);
            JSONObject siteObj = participant.getJSONObject("pinfoMap");
            String pid = participant.getString("pid");
            String uri = siteObj.getString("TEL");
            ParticipantInfo site = allParticipantMap.get(uri);
            if (site == null) {
                allParticipantMap.put(uri, new ParticipantInfo());
            }else if(site.getPId() != null && !site.getPId().equals(pid)){
//                add to repeat
                dealRepeat(site, participant);
                continue;
            }
            addUriAndPid(uri, pid);
            doParser(participant, false, true);
            //判断是否级联通道并添加
            createCasChannel(uri);
        }
    }

    private void dealRepeat(ParticipantInfo participantSite, JSONObject participant) {
        JSONObject participants = participant.getJSONObject("pinfoMap");
        String pid = participant.getString("pid");
        String uri = participants.getString("TEL");
        ParticipantInfo participantInfo = participantSite.getPIdToRepeatMap().get(pid);
        if(participantInfo == null){
            participantInfo = new ParticipantInfo();
            updateToRepeatMap(participantSite, participants, pid, uri, participantInfo);
            participantSite.getPIdToRepeatMap().put(pid, participantInfo);
        }else {
            //update
            updateToRepeatMap(participantSite, participants, pid, uri, participantInfo);
        }
    }

    private void updateToRepeatMap(ParticipantInfo participantSite, JSONObject participants, String pid, String uri, ParticipantInfo participantInfo) {
        //add
        Map<String, String> participantsInfoMap = new HashMap<>(16);
        if (uri == null) {
            uri = PID_TO_URI_MAP.get(pid);
        }
        String participantType = setWelinkUpSiteType(uri);
        participantsInfoMap.put("TYPE", participantType);
        participantsInfoMap.put("URI", uri);
        participantsInfoMap.put("PID", pid);
        //初始其他几个参数
        initParticipantsMap(participantsInfoMap, participants);
        dealBroadcastAndSpokesman(participantsInfoMap);
        if("0".equalsIgnoreCase(participantsInfoMap.get("STATE")) && !"0".equals(participantSite.getState())){
            //当前广播态为主通道
            if(!StringUtils.isEmpty(broadcastUri) && isUploadToSmcMaster(smcAccessCode, broadcastUri)){
                participantSite.setMultiPicInfo(mainChannelSource);
            }
        }
        dealSiteParticipant(participantInfo, participantsInfoMap);
        participantInfo.setParticipantID(uri);
        participantInfo.setPId(pid);
        participantInfo.setUri(uri);
    }


    public void createCasChannel(String uri) {
        if (isUploadToSmc(smcAccessCode,uri)) {
//            判断是否在线，进行级联通道初始化，锁定观看
            ParticipantInfo participantInfo = allParticipantMap.get(uri);

            Integer index;
            index = getCascadeCountFormUri(uri, smcAccessCode);
            if(maxCascadeNum.equals(index)){
                index = 0;
            }
            if (!cascadeChannelInfoMap.containsKey(uri)) {
                GwId gwId = null;
                if(conferenceSubscribeMessage != null){
                    gwId = conferenceSubscribeMessage.getTargetGwId();
                }else {
                    gwId = remoteGwId;
                }
                if(gwId != null){
                    CascadeParticipantParameter baseInfo = new CascadeParticipantParameter(gwId, index, CascadeParticipantDirection.UP);
                    CascadeChannelInfo cascadeChannelInfo = new CascadeChannelInfo(uri, baseInfo);
                    cascadeChannelInfoMap.put(uri, cascadeChannelInfo);
                    participantInfo.setVdcMarkCascadeParticipant(cascadeChannelInfo.getBaseInfo().toString());
                }
            }
            //判断在线,
//           ToDo MT号处理
            if("0".equals(participantInfo.getState()) && participantInfo.getMtNumber() != null && (!"COMMON".equals(confMode) || participantInfo.getMtNumber() > 1)){
                if(index == 0){
                    return;
                }
//                会场锁定状态
                if(participantInfo.getLockView() != null && participantInfo.getLockView()){
                    return;
                }
                try {
                    WeLinkMeetingControlService weLinkMeetingControlService = SpringUtil.getBean(WeLinkMeetingControlService.class);
                    // 锁定
                    LockViewParticipantsRequest lockViewParticipantsRequest = new LockViewParticipantsRequest(1);
                    String  pid = URI_TO_PID_MAP.get(uri);
                    weLinkMeetingControlService.lockViewParticipants(id, pid, lockViewParticipantsRequest, getConferenceToken());
                    // 观看
                    PartViewParticipantsRequest partViewParticipantsRequest = new PartViewParticipantsRequest();
                    partViewParticipantsRequest.setViewType(2);
                    partViewParticipantsRequest.setParticipantID(pid);
                    weLinkMeetingControlService.partViewParticipants(id, pid, partViewParticipantsRequest, getConferenceToken());
                } catch (MyHttpException exception) {
                    exception.printStackTrace();
                }
            }

        }
    }
    
    public void addUriAndPid(String uri, String pid) {
        if (StringUtils.isEmpty(uri) || StringUtils.isEmpty(pid)) {
            return;
        }
        URI_TO_PID_MAP.put(uri, pid);
        PID_TO_URI_MAP.put(pid, uri);
    }
    private boolean checkAttendReceived(JSONObject jsonObject) {
        if (!isAttendFullReceive) {
            queueCache.offer(jsonObject);
            return true;
        }
        return false;
    }

    public void dealParticipantIncreaseMessage(JSONObject jsonObject,String conferenceId) {
        if (checkAttendReceived(jsonObject)) return;
        JSONArray participants = jsonObject.getJSONArray("data");
        log.info("wss [dealParticipantIncreaseMessage] participants->" + participants);
        String LEAVE_SITE = "1";
        for (int i = 0; i < participants.size(); i++) {
            JSONObject participant = participants.getJSONObject(i);
            String pid = participant.getString("pid");
            String mode = participant.getString("mode");
            JSONObject siteJson = participant.getJSONObject("pinfoMap");
            String uri = "";
            if (siteJson != null && !siteJson.isEmpty()) {
                uri = siteJson.getString("TEL");
            }
            if (StringUtils.isEmpty(uri)) {
                uri = PID_TO_URI_MAP.get(pid);
            }
            if (StringUtils.isEmpty(uri)) {
                continue;
            }
            ParticipantInfo site = allParticipantMap.get(uri);
            if (LEAVE_SITE.equals(mode)) {
                if (site == null) {
                    continue;
                }
                Map<String, ParticipantInfo> pIdToRepeatMap = site.getPIdToRepeatMap();
                if(!pid.equals(site.getPId())){
                    pIdToRepeatMap.remove(pid);
                    continue;
                }
                //找在线的

                ParticipantInfo onLineParticipantInfo = null;
                ParticipantInfo firstParticipantInfo = null;
                for(ParticipantInfo participantInfo: pIdToRepeatMap.values()){
                    if(!"0".equals(participantInfo.getState())){
                        continue;
                    }
                    onLineParticipantInfo = participantInfo;
                    break;
                }
                if(onLineParticipantInfo == null){
                    onLineParticipantInfo = firstParticipantInfo;
                }
                if(onLineParticipantInfo != null){
                    cpParam(site, onLineParticipantInfo);
                    //删除repeat
                    pIdToRepeatMap.remove(onLineParticipantInfo.getPId());
                    addUriAndPid(uri, site.getPId());
                    continue;
                }

                site.setPId(null);
                dealLeaveSite(site, pid);
                continue;
            }
            if (site == null) {
                //如果不请自来会场没有name，直接抛弃
                String name = siteJson.getString("NAME");
                if(StringUtils.isEmpty(name)){
                    continue;
                }
                site = new ParticipantInfo();
                allParticipantMap.put(uri, site);
            }else if(site.getPId() != null && !pid.equals(site.getPId()) && "0".equals(site.getState())){
                //add to repeat
                dealRepeat(site, participant);
                continue;
            }else if(site.getPId() != null && !pid.equals(site.getPId()) && !"0".equals(site.getState())){
                dealRepeat(site, participant);
                //互换信息,找在线的
                Map<String, ParticipantInfo> pIdToRepeatMap = site.getPIdToRepeatMap();
                ParticipantInfo onLineParticipantInfo = null;
                for(ParticipantInfo participantInfo: pIdToRepeatMap.values()){
                    if(!"0".equals(participantInfo.getState())){
                        continue;
                    }
                    onLineParticipantInfo = participantInfo;
                    break;
                }
                if(onLineParticipantInfo != null){
                    ParticipantInfo cp = new ParticipantInfo();
                    cpParam(cp, site);
                    cpParam(site, onLineParticipantInfo);
                    //删除repeat
                    pIdToRepeatMap.remove(onLineParticipantInfo.getPId());
                    pIdToRepeatMap.put(cp.getPId(), cp);
                    addUriAndPid(uri, site.getPId());
                }
                continue;
            }

            addUriAndPid(uri, pid);
            if (site.getParticipantID() == null) {
                site.setParticipantID(uri);
            }
            site.setPId(pid);
            doParser(participant, false, false);
            siteShareNotify(site);
        }
        addCasChannel();
        dealAllParticipantMap(conferenceId);
    }

    private void cpParam(ParticipantInfo site, ParticipantInfo onLineParticipantInfo) {
        //属性交换
        site.setPId(onLineParticipantInfo.getPId());
        site.setLockView(onLineParticipantInfo.getLockView());
        site.setShare(onLineParticipantInfo.getShare());
        site.setState(onLineParticipantInfo.getState());
        site.setMute(onLineParticipantInfo.getMute());
        site.setUserUUID(onLineParticipantInfo.getUserUUID());
        site.setName(onLineParticipantInfo.getName());
        site.setRole(onLineParticipantInfo.getRole());
        site.setVideo(onLineParticipantInfo.getVideo());
        site.setAttendeeType(onLineParticipantInfo.getAttendeeType());
        site.setAccountId(onLineParticipantInfo.getAccountId());
        site.setPhone(onLineParticipantInfo.getPhone());
        site.setPhone2(onLineParticipantInfo.getPhone2());
        site.setPhone3(onLineParticipantInfo.getPhone3());
        site.setEmail(onLineParticipantInfo.getEmail());
        site.setSms(onLineParticipantInfo.getSms());
        site.setMtNumber(onLineParticipantInfo.getMtNumber());
        site.setDeptName(onLineParticipantInfo.getDeptName());
        site.setStatus(Status.MOD_ON_CHANGE);
    }

    //处理离线
    private void dealLeaveSite(ParticipantInfo participantSite, String pid) {
        //更新级联通道
        participantSite.setMute(true);
        if(!participantSite.isAttend()){
            log.info("wss [dealLeaveSite] DEL success pid->{} phone->{}", pid, participantSite.getPhone());
            //移除广播,辅流
            removeBroadOrPresenter(participantSite);
            if (allParticipantMap.containsKey(participantSite.getUri())) {
                participantSite = allParticipantMap.get(participantSite.getUri());
                participantSite.setState("1");
            }
            participantSite.setStatus(Status.MOD_ON_CHANGE);
            participantSite.setLockView(false);
            allParticipantMap.remove(participantSite.getUri());
            ONLINE_URI_MAP.remove(participantSite.getUri());
            //发送通知
            sendNotify(Collections.singletonList(participantSite.toParticipantStatusInfo(id)),2,id, conferenceSubscribeMessage);
        }else{
            log.info("wss [dealLeaveSite] MOD_ON_CHANGE success pid->{} phone->{}", pid, participantSite.getPhone());
            //移除广播,辅流
            removeBroadOrPresenter(participantSite);
            participantSite.setStatus(Status.MOD_ON_CHANGE);
            participantSite.setState("1");
            //发送通知
            sendNotify(Collections.singletonList(participantSite.toParticipantStatusInfo(id)),3,id, conferenceSubscribeMessage);
            participantSite.setStatus(Status.ON_NONE);
        }
        if(participantSite.getUri().equals(broadcastUri)){
            broadcastUri = null;
        }
        removeUriAndPid(participantSite.getPhone(), pid);
    }

    private void removeBroadOrPresenter(ParticipantInfo participantSite) {
        boolean isSendConfNotify = false;
        if(participantSite.getUri().equals(broadcastUri)){
            broadcastUri = "";
            sendCancelBroadcast();
            isSendConfNotify = true;
        }
        if(participantSite.getUri().equals(presenterId)){
            presenterId = "";
            participantSite.setShare(false);
            isSendConfNotify = true;
        }
        if(isSendConfNotify){
            sendConferenceNotify(true);
            MultiPicInfo multiPicInfo = new MultiPicInfo();
            multiPicInfo.setPicNum(1);
            multiPicInfo.setSubPicList(Collections.singletonList(new SubPic("","","",0)));
            sendNotify(Collections.singletonList(participantSite.toParticipantStatusInfo(id, multiPicInfo)), 3, id, conferenceSubscribeMessage);
        }
    }

    public void sendAllParticipantsBroadcast(String broadcastUri,String changeParticipantId) {
        List<ParticipantStatusInfo> changeList = new ArrayList<>();
        if (changeParticipantId == null || changeParticipantId.equals(this.broadcastUri)) {
            List<ParticipantInfo> onlineParticipants = getOnlineParticipants();
            for (ParticipantInfo participantInfo : onlineParticipants) {
                if(participantInfo.getVdcMarkCascadeParticipant()!= null){
                    continue;
                }
                if(!StringUtils.isEmpty(broadcastUri) && broadcastUri.equals(participantInfo.getUri())){
                    continue;
                }
                if (broadcastUri == null || "".equals(broadcastUri)) {
                    participantInfo.setMultiPicInfo();
                }else{
                    if("00000000-0000-0000-0000-000000000000".equals(broadcastUri)){
                        participantInfo.setMultiPicInfos();
                    }else{
                        participantInfo.setMultiPicInfo(broadcastUri);
                    }
                }
                changeList.add(participantInfo.toParticipantStatusInfo(id));
            }
            sendNotify(changeList, 3, id, conferenceSubscribeMessage);
        }
    }

    public List<ParticipantInfo> getOnlineParticipants() {
        //筛选在线会场
        return allParticipantMap.values().stream().filter(item -> "0".equals(item.getState())).collect(Collectors.toList());
    }

    private void removeUriAndPid(String uri, String pid) {
        URI_TO_PID_MAP.remove(uri);
        PID_TO_URI_MAP.remove(pid);
        if (!isUploadToSmc(smcAccessCode, uri) && ONLINE_URI_MAP.containsKey(uri)) {
            ONLINE_URI_MAP.put(uri, false);
        }
    }

    public void sendCancelBroadcast(){
        if(subscribeMessage == null){
            log.warn("subscribeMessage is empty.");
            return;
        }
        CascadeChannelNotifyInfo cascadeChannelNotifyInfo = new CascadeChannelNotifyInfo();
        cascadeChannelNotifyInfo.setConfCasId(smcAccessCode);
        //本级的会议号  用于远端查找
        cascadeChannelNotifyInfo.setRemoteConferenceId(id);
        MessageContent message = new MessageContent(MessageType.CANCEL_BROADCAST, JSON.toJSONString(cascadeChannelNotifyInfo));
        sendNotify(JSON.toJSONString(message));
    }

    public void dealInviteMessage(JSONArray siteInfo) {
        log.info("[InviteResultNotify]: " + siteInfo);
        List<InviteResultDto> results = siteInfo.toJavaList(InviteResultDto.class);
        //传进来为空不作处理
        if (null == results || results.isEmpty()) {
            log.error("dealInviteMessage siteInfo is Empty");
            return;
        }
        for (InviteResultDto inviteResultDto : results) {
            updateSiteInvite(inviteResultDto);
        }
    }

    private void updateSiteInvite(InviteResultDto inviteResultDto) {
        String uri = inviteResultDto.getCallNumber();
        String errorCode = inviteResultDto.getResultCode();
        if (StringUtils.isEmpty(uri)) {
            log.error("[updateSiteInvite] fail, uri is empty");
        }
        if ("0".equals(errorCode)) {
            return;
        }
        ParticipantInfo participantSite = null;
        try {
            ParticipantInfo participantInfo = allParticipantMap.get(uri);
            if (participantInfo == null) {
                return;
            }
            participantSite = (ParticipantInfo) participantInfo.clone();
        } catch (Exception e) {
            log.error("[updateSiteInvite] fail");
        }
        if (participantSite != null) {
            Integer resultErrorCode = getSmcErrorCode(errorCode);
            if (resultErrorCode == null) {
                return;
            }

            participantSite.setCallFailReason(resultErrorCode);
//            participantSite.setState("1");
            participantSite.setStatus(Status.MOD_ON_CHANGE);
            sendNotify(Collections.singletonList(participantSite.toParticipantStatusInfo(id)),3,id, conferenceSubscribeMessage);
            participantSite.setStatus(Status.ON_NONE);
        }
    }


    public void sendNotify(List<ParticipantStatusInfo> changeList,int type,String conferenceId, SubscribeMessage subscribeMessage) {
        ParticipantStatusNotify participantInfoNotify = new ParticipantStatusNotify();
        participantInfoNotify.setConfCasId(this.accessCode);
        participantInfoNotify.setType(type);
        participantInfoNotify.setSize(changeList.size());
        participantInfoNotify.setConferenceId(conferenceId);
        participantInfoNotify.setChangeList(changeList);
        subscribeMessage.setMessage(JSON.toJSONString(participantInfoNotify));
        MessageContent messageContent = new MessageContent();
        messageContent.setBody(JSON.toJSONString(subscribeMessage));
        messageContent.setType(MessageType.SUBSCRIBE_MESSAGE);
        sendNotify(JSON.toJSONString(messageContent));
    }

    public void sendNotify(String messageContent) {
        WebSocket webSocket = WelinkWebSocketClient.getWebSocket();
        log.info("ws send message:{}", messageContent);
        if (webSocket == null) {
            synchronized (this) {
                //连接conf websocket
                webSocket = WelinkWebSocketClient.getWebSocket();
                if (webSocket == null) {
                    webSocket = WelinkWebSocketClient.openNodeWebsocket();
                    log.info("ws connect success");
                }
            }
        }
        webSocket.sendText(messageContent);
        log.info("ws send message success");
    }

    private static final int SUFFIX_DIGITS = 3;
    public static boolean isUploadToSmc(String smcAccess,String welinkSiteUri){
        boolean flag = false;
        if((smcAccess.length() == welinkSiteUri.length()) && smcAccess.equals(welinkSiteUri)){
            return true;
        }
        String tempSmcSuffix;
        if(welinkSiteUri.length() <= SUFFIX_DIGITS){
            return false;
        }
        //截取前面
        tempSmcSuffix = welinkSiteUri.substring(0,welinkSiteUri.length()-SUFFIX_DIGITS);
        if(tempSmcSuffix.startsWith(smcAccess)){
            flag = true;
        }
        return flag;
    }
    private static final int MAX_CASCADENUM = 64;
    //判断是不是welink第一条主会场
    public static boolean isUploadToSmcMaster(String smcAccess,String welinkSiteUri){
        String tempNum;
        //取长度，==SUFFIX_DIGITS，
        if(smcAccess.length() == welinkSiteUri.length() && smcAccess.equals(welinkSiteUri)){
            return true;
        }
        //截取最后三位，大于64的
        tempNum = welinkSiteUri.substring(welinkSiteUri.length()-SUFFIX_DIGITS);
        return Integer.parseInt(tempNum) > MAX_CASCADENUM;
    }
    //通过会场的num数字取得通道数量信息
    public static Integer getCascadeCountFormUri(String welinkMasterUri,String smcAccessCode){
        int cascadeNum = 1;
        if(smcAccessCode.length() == welinkMasterUri.length() && smcAccessCode.equals(welinkMasterUri)){
            return cascadeNum;
        }
        String tempWelinkNum = welinkMasterUri.substring(welinkMasterUri.length()-SUFFIX_DIGITS);
        cascadeNum = Integer.parseInt(tempWelinkNum);
        if(cascadeNum >MAX_CASCADENUM){
            cascadeNum = cascadeNum - MAX_CASCADENUM;
        }
        return cascadeNum;
    }

    public CasChannelPartyUtil casChannelParty = new CasChannelPartyUtil();
    public synchronized void setCasChannelMaxNumber(int maxNumber) {
        if (maxNumber > 9) {
            maxNumber = 9;
        }
        this.maxCascadeNum = maxNumber;
    }
    public void addCasChannel() {
        if (maxCascadeNum == null) {
            maxCascadeNum = 0;
        }
        int currentChannelNum = cascadeChannelInfoMap.size();//当前级联通道数量
        if (getCurrentOnlineNum() <= currentChannelNum) {
            //当前在线会场小于级联通道数量 无需添加
            return;
        }
        int max = Math.min(9, maxCascadeNum);
        int min = Math.min(getCurrentOnlineNum(), max);
        int addChannelSize = min - currentChannelNum; //需要添加的级联通道数量

        if (addChannelSize > 0) {
            String[] resultArray = null;
            try {
                resultArray = weLinkWebSocketService.getConferencePwd(id);
            } catch (MyHttpException exception) {
                exception.printStackTrace();
            }
            if(resultArray == null){
                return;
            }
            CascadeChannelMessage channelMessage = new CascadeChannelMessage(smcAccessCode,accessCode,name,addChannelSize, min, resultArray[1]);
            MessageContent message = new MessageContent(MessageType.ADD_WELINK_CASCADE_CHANNEL, JSON.toJSONString(channelMessage));
//            WelinkScheduler.getInstance().scheduledThreadPoolLaterDo(0, TimeUnit.SECONDS, () -> sendNotify(message));
            sendNotify(JSON.toJSONString(message));
        }
    }

    public ScheduleConfBrief toScheduleConfBrief() {
        ScheduleConfBrief scheduleConfBrief = new ScheduleConfBrief();
        scheduleConfBrief.setId(id);
        scheduleConfBrief.setAccessCode(accessCode);
        scheduleConfBrief.setSubject(name);
        return scheduleConfBrief;
    }

    public void siteShareNotify(ParticipantInfo participantInfo ) {
        if (participantInfo.getShare() != null && participantInfo.getShareChange() != null && participantInfo.getShareChange()) {
            participantInfo.setShareChange(false);
            if (participantInfo.getShare()) {
                presenterId = participantInfo.getPhone();
            } else {
                presenterId = "";
            }
            sendConferenceNotify(false);
            if(!participantInfo.getShare()){
                presenterId = null;
            }

        }
    }

    public String getSiteShare() {
        for(ParticipantInfo participantInfo: allParticipantMap.values()){
            if(participantInfo.getState() == null || !"0".equalsIgnoreCase(participantInfo.getState())){
                continue;
            }
            if(participantInfo.getShare() != null && participantInfo.getShare()){
                return participantInfo.getUri();
            }
        }
        return null;
    }

    public Integer getSmcErrorCode(String errorCode) {
        Integer smcErrorCode = null;
        try {
            switch (errorCode) {
                case "11076005"://会场无响应
                case "11076014"://SIP呼叫超时
                    smcErrorCode = 129;
                    break;
                case "11076006"://会场不存在
                    smcErrorCode = 110;
                    break;
                case "11076007"://会场离线
                    smcErrorCode = 109;
                    break;
                case "11076008"://会场正忙
                    smcErrorCode = 117;
                    break;
                case "11076009"://会场拒绝接听
                    smcErrorCode = 115;
                case "11076010"://会场挂断
                    smcErrorCode = 196622;
                    break;
                case "11076001"://会场被终端主持人挂断
                case "11076002"://会场被portal管理员挂断
                case "11076016":
                    smcErrorCode = 196621;
                    break;
                case "11076003"://超出VMR最大与会方数
                case "11076004"://企业并发数不足
                case "11076011"://媒体资源不足
                case "11076012"://PSTN服务未开通
                case "11076013"://申请地址本鉴权信息失败
                default:
                    smcErrorCode = 100;
                    break;
            }
        } catch (Exception e) {
            log.error("get smc error code error,message:{},exception:{}",e.getMessage(), e);
        }
        return smcErrorCode;
    }

    public void dealSpeakStatus(JSONArray data) {
        try {
            String oldSpokesmanId = spokesmanId;
            List<Speaker> newSpeakers = data.toJavaList(Speaker.class);
            if (null == newSpeakers || newSpeakers.isEmpty()) {
                spokesmanId = null;
                //推送发言人为空
            } else {
                //找声音最大的
                Speaker currentSpeaker = newSpeakers.get(0);
                int currentVolume = Integer.parseInt(currentSpeaker.getSpeakingVolume());
                for (int i = 1; i < newSpeakers.size(); i++) {
                    Speaker speaker = newSpeakers.get(i);
                    int volume = Integer.parseInt(speaker.getSpeakingVolume());
                    if (currentVolume < volume) {
                        currentSpeaker = speaker;
                    }
                }
                spokesmanId = PID_TO_URI_MAP.get(currentSpeaker.getPid());
            }
            if((StringUtils.isEmpty(oldSpokesmanId) && StringUtils.isEmpty(spokesmanId))
                    || (!StringUtils.isEmpty(oldSpokesmanId) && oldSpokesmanId.equals(spokesmanId))
                    || (!StringUtils.isEmpty(spokesmanId) && spokesmanId.equals(oldSpokesmanId)) ){
                return;
            }
            sendConferenceNotify(false);
        }catch (Exception e){
            log.error("error msg: {}", e.getMessage());
            log.error("error stack: {}", (Object[]) e.getStackTrace());
        }

    }

    /**
     * websocket定时任务 5*10 = 50秒
     */
    public synchronized boolean refreshTokenTimeout() {
        boolean result = false;
        this.tokenStatus++;
        if (this.tokenStatus * 20 > this.confTokenExpireTime - 200) {
            this.tokenStatus = 0;
            result = true;
        }
        return result;
    }
}

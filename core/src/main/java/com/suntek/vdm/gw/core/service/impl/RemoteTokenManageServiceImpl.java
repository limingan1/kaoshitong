package com.suntek.vdm.gw.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.enums.CoreApiUrl;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.pojo.RemoteToken;
import com.suntek.vdm.gw.core.pojo.NodeKeepAliveInfo;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import com.suntek.vdm.gw.core.service.RemoteTokenManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;
import com.suntek.vdm.gw.welink.service.WeLinkTokenManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RemoteTokenManageServiceImpl implements RemoteTokenManageService {

    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private WeLinkTokenManageService weLinkTokenManageService;

    private static Interner<String> pool = Interners.newWeakInterner();

    Map<String, RemoteToken> tokenMap;

    Map<String, NodeKeepAliveInfo> nodeKeepAliveInfoMap = new ConcurrentHashMap<>();

    private Map<String, RemoteToken> getTokenMap() {
        if (tokenMap == null) {
            tokenMap = new ConcurrentHashMap<>();
        }
        return tokenMap;
    }

    @Override
    public RemoteToken get(String id) {
        return getTokenMap().get(id);
    }

    @Override
    public void add(String id, String token, String ip, boolean ssl, String areaCode, long expire) {
        RemoteToken remoteToken = new RemoteToken(id, token, ip, ssl, areaCode, expire);
        getTokenMap().put(id, remoteToken);
        try {
            keepAlive(id);
        } catch (BaseStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void replace(String newId, String oldId) {
        RemoteToken remoteToken = getTokenMap().remove(oldId);
        getTokenMap().put(newId, remoteToken);
    }


    @Override
    public void del(String id) {
        getTokenMap().remove(id);
    }

    @Override
    public String getToken(String id) {
        RemoteToken remoteToken = getTokenMap().get(id);
        if(remoteToken == null){
            return null;
        }
        return remoteToken.getToken();
    }


    @Override
    public boolean expired(String id) {
        if (contains(id)) {
            return expiredCheck(get(id).getTokenUpdateTime());
        }
        return false;
    }

    private boolean expiredCheck(long tokenUpdateTime) {
        long currentTime = System.currentTimeMillis();
        return expiredCheck(tokenUpdateTime, currentTime);
    }

    private boolean expiredCheck(long tokenUpdateTime, long currentTime) {
        return tokenUpdateTime + (CoreConfig.TOKEN_TIME_OUT * 1000) >= currentTime;
    }

    @Override
    public boolean contains(String id) {
        return getTokenMap().containsKey(id);
    }


    @Override
    public void keepAlive(String id) throws BaseStateException {
        RemoteToken remoteToken = get(id);
        if (remoteToken == null) {
            throw new BaseStateException("Remote token is  null");
        }
        if (!expiredCheck(remoteToken.getTokenUpdateTime())) {
            log.warn("Remote token expired [code:{}]", remoteToken.getAreaCode());
            throw new BaseStateException("Remote detection expired");
        }
        try {
            NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(id);
            if(NodeBusinessType.WELINK.equals(nodeBusinessType) || NodeBusinessType.CLOUDLINK.equals(nodeBusinessType)){
                String token = weLinkTokenManageService.updateToken();
                remoteToken.setToken(token);
                nodeManageService.setRemoteLoginMsg(id, "");
            }else{
                MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
                NodeData local = nodeDataService.getLocal();
                headers.set("RemoteStatus", local.getId());
                ResponseEntity<String> responseEntity = remoteGwService.toByGwId(new GwId(id, remoteToken.getAreaCode())).put(CoreApiUrl.KEEP_ALIVE.value(), null, headers);
                String result = responseEntity.getBody();
                KeepALiveResponse smcKeepAliveResponse = JSON.parseObject(result, KeepALiveResponse.class);
                if(smcKeepAliveResponse.getUuid() != null){
                    remoteToken.setToken(smcKeepAliveResponse.getUuid());
                }
                HttpHeaders httpHeaders = responseEntity.getHeaders();
                List<String> msgs = httpHeaders.get("Msg");
                if(msgs != null && msgs.size()>0){
                    String msg = msgs.get(0);
                    nodeManageService.setRemoteLoginMsg(id, msg);
                }
            }
            remoteToken.setTokenUpdateTime(System.currentTimeMillis());
        } catch (MyHttpException e) {
            log.warn("Remote token expired [code:{}] and error:{}", remoteToken.getAreaCode(), e.toString());
            del(id);
            throw new BaseStateException(e.getBody());
        } catch (Exception e) {
            log.error("exception", e);
            throw new BaseStateException(e.getMessage());
        }
    }


    @Override
    public void keepAliveAll() {
        List<NodeData> nodeData = nodeDataService.getNotLocal();
        if (nodeData != null && nodeData.size() > 0) {
            for (NodeData item : nodeData) {
                NodeKeepAliveInfo nodeKeepAliveInfo = getNodeKeepAliveInfoById(item.getId());
                try {
                    if (nodeKeepAliveInfo.need()) {
                        keepAlive(item.getId());
                    }
                } catch (BaseStateException e) {
                    nodeKeepAliveInfo.fail();
                    log.error("remote token Keep alive error:{}", e.getMessage());
                    //重新登录一次
                    try {
                        nodeManageService.loginNode(item);
                    } catch (MyHttpException myHttpException) {
                        myHttpException.printStackTrace();
                    }
                }
            }
        }
    }

    public void removeNodeKeepAliveInfoById(String nodeId) {
        nodeKeepAliveInfoMap.remove(nodeId);
    }

    public NodeKeepAliveInfo getNodeKeepAliveInfoById(String nodeId) {
        synchronized (pool.intern(nodeId)) {
            if (!nodeKeepAliveInfoMap.containsKey(nodeId)) {
                NodeData nodeData = nodeDataService.getOneById(nodeId);
                nodeKeepAliveInfoMap.put(nodeId, new NodeKeepAliveInfo(nodeId, nodeData.getName(), 0, 0,0));
            }
            NodeKeepAliveInfo nodeKeepAliveInfo = nodeKeepAliveInfoMap.get(nodeId);
            return nodeKeepAliveInfo;
        }
    }

    public Boolean triggerKeepAlive(NodeData nodeData){
        NodeKeepAliveInfo nodeKeepAliveInfo = getNodeKeepAliveInfoById(nodeData.getId());
        //请求触发keepAlive,5秒只触发一次
        if(System.currentTimeMillis() - nodeKeepAliveInfo.getLastTime() < 5*1000){
            return false;
        }
        try {
            nodeKeepAliveInfo.setLastTime(System.currentTimeMillis());
            keepAlive(nodeData.getId());
            return true;
        } catch (BaseStateException e) {
            e.printStackTrace();
            try {
                nodeManageService.loginNode(nodeData);
                return true;
            } catch (MyHttpException myHttpException) {
                myHttpException.printStackTrace();
            }
        }
        return false;
    }
}

package com.suntek.vdm.gw.core.service.impl;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.pojo.NodeKeepAliveInfo;
import com.suntek.vdm.gw.core.pojo.RemoteToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.VmNodeDataService;
import com.suntek.vdm.gw.core.service.VmNodeTokenManagerService;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class VmNodeTokenManagerServiceImpl implements VmNodeTokenManagerService {
    @Autowired
    private SmcLoginService smcLoginService;
    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private LocalTokenManageService localTokenManageService;

    Map<String, NodeKeepAliveInfo> nodeKeepAliveInfoMap = new ConcurrentHashMap<>();
    static Map<String, RemoteToken> tokenMap = new ConcurrentHashMap<>();

    private static Interner<String> pool = Interners.newWeakInterner();

    private Map<String, LocalToken> getLocalTokenMap() {
        return localTokenManageService.getTokenMap();
    }

    private Map<String, RemoteToken> getTokenMap() {
        return tokenMap;
    }

    @Override
    public RemoteToken get(String id) {
        return getTokenMap().get(id);
    }

    @Override
    public void add(String id, String token, String areaCode, Long expire, String username) {
        RemoteToken remoteToken = new RemoteToken(id, token, "127.0.0.1", true, areaCode, expire);
        getTokenMap().put(id, remoteToken);
        localTokenManageService.add(token, username, token, expire, false, null);
        try {
            keepAlive(id);
        } catch (BaseStateException | MyHttpException e) {
            e.printStackTrace();
        }
    }

    public void del(String id) {
        RemoteToken remoteToken = getTokenMap().get(id);
        if(remoteToken != null){
            localTokenManageService.del(remoteToken.getToken());
        }
        getTokenMap().remove(id);


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


    public boolean contains(String id) {
        return getTokenMap().containsKey(id);
    }


    public void keepAlive(String id) throws BaseStateException, MyHttpException {
        RemoteToken vmToken = get(id);
        if (vmToken == null) {
            throw new BaseStateException("vmToken token is  null");
        }
        if (!expiredCheck(vmToken.getTokenUpdateTime())) {
            log.warn("vmToken token expired [code:{}]", vmToken.getAreaCode());
            throw new BaseStateException("vmToken detection expired");
        }
        if (vmToken.isExpire()) {
            del(id);
            throw new BaseStateException("vmToken has expired");
        }
        try {
            KeepALiveResponse response = smcLoginService.keepAlive(vmToken.getToken());
            long expire = Long.parseLong(response.getExpire());
            LocalToken localToken = localTokenManageService.get(vmToken.getToken());
            localToken.setExpire(expire);
            vmToken.setExpire(expire);
            vmToken.setTokenUpdateTime(System.currentTimeMillis());
        } catch (MyHttpException e) {
            del(id);
            log.info("Keep alive local token fail,error:{}", e.toString());
            throw e;
        }
    }



    public void keepAliveAll() {
        List<VmNodeData> vmNodeDatas = vmNodeDataService.getAll();
        if (vmNodeDatas != null && vmNodeDatas.size() > 0) {
            for (VmNodeData item : vmNodeDatas) {
                NodeKeepAliveInfo nodeKeepAliveInfo = getNodeKeepAliveInfoById(item.getId());
                try {
                    if (nodeKeepAliveInfo.need()) {
                        keepAlive(item.getId());
                    }
                } catch (BaseStateException | MyHttpException e) {
                    nodeKeepAliveInfo.fail();
                    log.error("vm token Keep alive error:{}", e.getMessage());
                    //重新登录一次
                    try {
                        nodeManageService.loginVmNode(item);
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
                VmNodeData nodeData = vmNodeDataService.getOneById(nodeId);
                nodeKeepAliveInfoMap.put(nodeId, new NodeKeepAliveInfo(nodeId, nodeData.getName(), 0, 0,0));
            }
            NodeKeepAliveInfo nodeKeepAliveInfo = nodeKeepAliveInfoMap.get(nodeId);
            return nodeKeepAliveInfo;
        }
    }

    public Boolean triggerKeepAlive(VmNodeData vmNodeData){
        NodeKeepAliveInfo nodeKeepAliveInfo = getNodeKeepAliveInfoById(vmNodeData.getId());
        //请求触发keepAlive,5秒只触发一次
        if(System.currentTimeMillis() - nodeKeepAliveInfo.getLastTime() < 5*1000){
            return false;
        }
        try {
            nodeKeepAliveInfo.setLastTime(System.currentTimeMillis());
            keepAlive(vmNodeData.getId());
            return true;
        } catch (BaseStateException | MyHttpException e) {
            e.printStackTrace();
            try {
                nodeManageService.loginVmNode(vmNodeData);
                return true;
            } catch (MyHttpException myHttpException) {
                myHttpException.printStackTrace();
            }
        }
        return false;
    }

}

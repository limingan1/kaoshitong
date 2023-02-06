package com.suntek.vdm.gw.core.service.orgUser;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.CoreApiUrl;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.entity.OrgUserData;
import com.suntek.vdm.gw.core.pojo.NodeKeepAliveInfo;
import com.suntek.vdm.gw.core.pojo.RemoteToken;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OrgUserTokenManagerServiceImpl implements OrgUserTokenManagerService {
    @Autowired
    private OrgUserDataService orgUserDataService;
    @Autowired
    @Lazy
    private OrgUserManagerService orgUserManagerService;
    @Qualifier("httpServiceImpl")
    @Autowired
    private HttpService httpService;

    @Value("${cas.service.port}")
    private String https_port;

    @Value("${cas.service.http-port}")
    private String http_port;




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
    public void add(String id, String token, String ip, boolean ssl, String areaCode, long expire){
        RemoteToken remoteToken = new RemoteToken(id, token, ip, ssl, areaCode, expire);
        getTokenMap().put(id, remoteToken);
        try {
            keepAlive(id);
        } catch (BaseStateException e) {
            e.printStackTrace();
        }
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
    public void replace(String newId, String oldId) {

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
    public String urlSplice(String ip, boolean ssl) {
        StringBuilder sb = new StringBuilder();
        sb.append(ssl ? "https://" : "http://");
        sb.append(ip);
        sb.append(":");
        sb.append(ssl ? https_port : http_port);
        return sb.toString();
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
            MultiValueMap<String, String> headers=new LinkedMultiValueMap<>();
            headers.set("Token", remoteToken.getToken());
            String httpResponse = httpService.put(urlSplice(remoteToken.getIp(), remoteToken.isSsl()) + CoreApiUrl.GET_TOKEN.value(), null,headers).getBody();
            KeepALiveResponse keepAliveResponse = JSON.parseObject(httpResponse, KeepALiveResponse.class);
            if(keepAliveResponse.getUuid() != null){
                remoteToken.setToken(keepAliveResponse.getUuid());
            }
            remoteToken.setExpire(Long.valueOf(keepAliveResponse.getExpire()));
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
        List<OrgUserData> orgUserDatas = orgUserDataService.getAll();
        if (orgUserDatas != null && orgUserDatas.size() > 0) {
            for (OrgUserData item : orgUserDatas) {
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
                        orgUserManagerService.add(item.getId());
                    } catch (MyHttpException myHttpException) {
                        myHttpException.printStackTrace();
                    }
                }
            }
        }

    }

    @Override
    public boolean contains(String id) {
        return getTokenMap().containsKey(id);
    }

    @Override
    public NodeKeepAliveInfo getNodeKeepAliveInfoById(String id) {
        synchronized (pool.intern(id)) {
            if (!nodeKeepAliveInfoMap.containsKey(id)) {
                OrgUserData orgUserData = orgUserDataService.getOneById(id);
                nodeKeepAliveInfoMap.put(id, new NodeKeepAliveInfo(id, orgUserData.getName(), 0, 0,0));
            }
            NodeKeepAliveInfo nodeKeepAliveInfo = nodeKeepAliveInfoMap.get(id);
            return nodeKeepAliveInfo;
        }
    }

    @Override
    public void removeNodeKeepAliveInfoById(String nodeId) {
        nodeKeepAliveInfoMap.remove(nodeId);
    }

    @Override
    public Boolean triggerKeepAlive(OrgUserData orgUserData) {
        NodeKeepAliveInfo nodeKeepAliveInfo = getNodeKeepAliveInfoById(orgUserData.getId());
        //请求触发keepAlive,5秒只触发一次
        if(System.currentTimeMillis() - nodeKeepAliveInfo.getLastTime() < 5*1000){
            return false;
        }
        try {
            nodeKeepAliveInfo.setLastTime(System.currentTimeMillis());
            keepAlive(orgUserData.getId());
            return true;
        } catch (BaseStateException e) {
            e.printStackTrace();
            try {
                orgUserManagerService.add(orgUserData.getId());
                return true;
            } catch (MyHttpException myHttpException) {
                myHttpException.printStackTrace();
            }
        }
        return false;
    }
}

package com.suntek.vdm.gw.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.common.enums.NodeStatusType;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.VmNodeDataService;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import com.suntek.vdm.gw.smc.service.SmcOtherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LocalTokenManageServiceImpl implements LocalTokenManageService {
    @Autowired
    private SmcLoginService smcLoginService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private SmcOtherService smcOtherService;
    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private NodeDataService nodeDataService;
    @Value("${localOrgNodeDisplay}")
    private boolean localOrgNodeDisplay;

    static Map<String, LocalToken> tokenMap = new ConcurrentHashMap<>();

    public static long timeDifference = 0;

    @Override
    public Map<String, LocalToken> getTokenMap() {
        return tokenMap;
    }

    static Map<String, String> NODE_ID_TOKEN_MAP = new ConcurrentHashMap<>();

    public Map<String, String> getNODE_ID_TOKEN_MAP() {
        return NODE_ID_TOKEN_MAP;
    }


    @Override
    public LocalToken get(String token) {
        return getTokenMap().get(token);
    }

    @Override
    public GwId getRealLocalGwIdByToken(String token){
        LocalToken localToken = get(token);
        if (localToken == null) {
            log.error("local uu_id is null: token:{}", token);
            return null;
        }
        GwId gwId = localToken.getGwId();
        if (gwId == null) {
            return nodeDataService.getLocal().toGwId();
        } else {
            return gwId;
        }
    }

    @Override
    public LocalToken getByNodeId(String nodeId) {
        String token = getNODE_ID_TOKEN_MAP().get(nodeId);
        if (token == null) {
            return null;
        }
        return get(token);
    }

    @Override
    public void delByNodeId(String nodeId) {
        String token = getNODE_ID_TOKEN_MAP().get(nodeId);
        if (token == null) {
            return;
        }
        getTokenMap().remove(token);
        log.info("NODE_ID_TOKEN_MAP remove nodeId:{}", nodeId);
        getNODE_ID_TOKEN_MAP().remove(nodeId);
    }

    @Override
    public String getNodeId(String token) {
        Iterator iterator = getNODE_ID_TOKEN_MAP().keySet().iterator();
        while (iterator.hasNext()) {
            String nodeId = iterator.next().toString();
            String nodeToken = getNODE_ID_TOKEN_MAP().get(nodeId);
            if (nodeToken.equals(token)) {
                return nodeId;
            }
        }
        return null;
    }


    @Override
    public void add(String token, String username, String smcToken, long expire,boolean fromClientLogin, GwId realLocalGwId) {
        LocalToken localToken = new LocalToken(token, username, smcToken, expire);
        getTokenMap().put(token, localToken);
        log.info("add user token: {}", localToken.getUsername());
        if(CoreConfig.INTERNAL_USER_TOKEN.equals(token)){
            getTokenMap().put(smcToken, localToken);
            setCode(smcToken, "local");
        }
        //获取user OrgId
        String orgId = getUserOrgId(localToken);
        localToken.setOrgId(orgId);
        if (realLocalGwId != null) {
            log.info("set local gwid 0:{}", realLocalGwId);
            localToken.setGwId(nodeDataService.getLocal().toGwId());
            return;
        }
        VmNodeData vmNodeData = vmNodeDataService.getOneByOrgId(orgId);
        if(vmNodeData == null){
            if (!fromClientLogin || localOrgNodeDisplay) {
                log.info("set local gwid 1:{}", nodeDataService.getLocal().toGwId());
                localToken.setGwId(nodeDataService.getLocal().toGwId());
            } else {
                VmNodeData rootVmNode = vmNodeDataService.getRootVmNode();
                log.info("set local gwid 2:{}", rootVmNode != null ? rootVmNode.toGwId() : nodeDataService.getLocal().toGwId());
                localToken.setGwId(rootVmNode != null ? rootVmNode.toGwId() : nodeDataService.getLocal().toGwId());
            }
            return;
        }
        log.info("set local gwid 3:{}", vmNodeData.toGwId());
        localToken.setGwId(vmNodeData.toGwId());
    }

    private String getUserOrgId(LocalToken localToken){
        String respon = null;
        try {
            respon = smcOtherService.getUserOrgId(localToken.getUsername(), localToken.getSmcToken());
        } catch (MyHttpException exception) {
            exception.printStackTrace();
        }
        JSONObject jsonObject = JSON.parseObject(respon);
        if(jsonObject == null){
            return null;
        }
        JSONObject account = jsonObject.getJSONObject("account");
        if(account == null){
            return null;
        }
        JSONObject organization = account.getJSONObject("organization");
        if(organization == null){
            return null;
        }
        return organization.getString("id");
    }

    @Override
    public void del(String token) {
        LocalToken localToken = getTokenMap().remove(token);
        if(localToken != null){
            log.info("delete user token: {}", localToken.getUsername());
        }
        Iterator iterator = getNODE_ID_TOKEN_MAP().keySet().iterator();
        while (iterator.hasNext()) {
            String nodeId = iterator.next().toString();
            String nodeToken = getNODE_ID_TOKEN_MAP().get(nodeId);
            if (nodeToken.equals(token)) {
                nodeManageService.setNodeInStatus(nodeId, NodeStatusType.OFFLINE);
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public boolean expired(String token) {
        if (contains(token)) {
            LocalToken localToken = get(token);
            return localToken.isExpire();
        }
        return true;
    }


    @Override
    public String getSmcToken(String token) {
        LocalToken localToken = get(token);
        if (localToken == null) {
            return null;
        }
        return localToken.getSmcToken();
    }

    @Override
    public void setCode(String token, String nodeId) {
        log.info("NODE_ID_TOKEN_MAP set nodeId:{} gwHttpId:{}", nodeId, token);
        if (nodeId != null) {
            getNODE_ID_TOKEN_MAP().put(nodeId, token);
        }
    }

    @Override
    public boolean contains(String token) {
        return getTokenMap().containsKey(token);
    }

    @Override
    public List<String> getExpiredToken() {
        List<String> result = new ArrayList<>();
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<String, LocalToken> entry : getTokenMap().entrySet()) {
            LocalToken localToken = get(entry.getKey());
            if (localToken.isExpire()) {
                result.add(entry.getKey());
            }
        }
        return result;
    }


    @Override
    public KeepALiveResponse keepAlive(String token) throws BaseStateException, MyHttpException {
        if (token == null) {
            throw new BaseStateException("token is null");
        }
        LocalToken localToken = get(token);
        if (localToken != null) {
            if (localToken.isExpire()) {
                del(token);
                throw new BaseStateException("token has expired");
            }
            try {
                KeepALiveResponse response = smcLoginService.keepAlive(localToken.getSmcToken());
                localToken.setExpire(Long.valueOf(response.getExpire()));
                localToken.setSmcToken(response.getUuid());
                return response;
            } catch (MyHttpException e) {
                del(token);
                log.info("Keep alive local token fail,error:{}", e.toString());
                throw e;
            }
        } else {
            throw new BaseStateException("token not found");
        }
    }

    @Override
    public void cleanExpired() {
        //过期的token
        List<String> invalidationToken = getExpiredToken();
        for (String item : invalidationToken) {
            log.info("clean local expired gwHttpId:{}", item);
            del(item);
        }
    }

}

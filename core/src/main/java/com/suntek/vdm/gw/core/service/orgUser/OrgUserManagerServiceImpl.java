package com.suntek.vdm.gw.core.service.orgUser;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.CoreApiUrl;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.util.AuthorizationUtil;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.entity.OrgUserData;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Slf4j
@Service
public class OrgUserManagerServiceImpl implements OrgUserManagerService {
    @Autowired
    private OrgUserDataService orgUserDataService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private OrgUserTokenManagerService orgUserTokenManagerService;
    @Qualifier("httpServiceImpl")
    @Autowired
    private HttpService httpService;


    @Value("${cas.service.port}")
    private String https_port;

    @Value("${cas.service.http-port}")
    private String http_port;


    @Override
    public void add(String id) throws MyHttpException {
        OrgUserData orgUserData = orgUserDataService.getOneById(id);
        NodeData nodeData = nodeDataService.getOneById(orgUserData.getNodeId());
        try {
            String authorization = AuthorizationUtil.getAuthorization(orgUserData.getUsername(), orgUserData.decryptPassword());
            GetTokenResponse getTokenResponse = loginRemote(nodeData.getId(), authorization);
            orgUserTokenManagerService.add(id, getTokenResponse.getUuid(), nodeData.getIp(), nodeData.isHttps(),nodeData.getAreaCode(),Long.valueOf(getTokenResponse.getExpire()));
        }catch (MyHttpException e){
            throw e;
        }
        catch (Exception e) {
            log.error("login OrgUser error: {}",e.getStackTrace());
        }
    }

    @Override
    public GetTokenResponse loginRemote(String id, String authorization) throws MyHttpException {
        NodeData nodeData = nodeDataService.getOneById(id);
        MultiValueMap<String, String> headers=new LinkedMultiValueMap<>();
        headers.set("Authorization",authorization);
        String httpResponse = httpService.get(urlSplice(nodeData.getIp(), nodeData.isHttps()) + CoreApiUrl.GET_TOKEN.value(), null,headers).getBody();
        GetTokenResponse getTokenResponse = JSON.parseObject(httpResponse, GetTokenResponse.class);
        return getTokenResponse;
    }

    @Override
    public void update(String id) throws MyHttpException {
        add(id);
    }

    @Override
    public void del(String id) {
        orgUserTokenManagerService.del(id);
    }

    public String urlSplice(String ip, boolean ssl) {
        StringBuilder sb = new StringBuilder();
        sb.append(ssl ? "https://" : "http://");
        sb.append(ip);
        sb.append(":");
        sb.append(ssl ? https_port : http_port);
        return sb.toString();
    }
}

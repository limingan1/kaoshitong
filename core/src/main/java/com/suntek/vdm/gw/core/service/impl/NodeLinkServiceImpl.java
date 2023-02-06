package com.suntek.vdm.gw.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.neovisionaries.ws.client.WebSocket;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.util.AuthorizationUtil;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.core.api.request.node.GetNodeTokenRequest;
import com.suntek.vdm.gw.core.api.response.node.GetNodeTokenResponse;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.enums.CoreApiUrl;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.NodeLinkService;
import com.suntek.vdm.gw.core.service.RemoteTokenManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.ws.client.WebsocketClient;
import com.suntek.vdm.gw.smc.ws.stomp.websocket.SmcWebsocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;

@Slf4j
@Service
public class NodeLinkServiceImpl implements NodeLinkService {

    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private LocalTokenManageService localTokenManageService;

    @Value("${cas.service.port}")
    private String https_port;

    @Value("${cas.service.http-port}")
    private String http_port;

    @Autowired
    private RemoteTokenManageService remoteTokenManageService;

    @Autowired
    @Qualifier("httpServiceImpl")
    private HttpService httpService;

    public String urlSplice(String ip, boolean ssl) {
        StringBuilder sb = new StringBuilder();
        sb.append(ssl ? "https://" : "http://");
        sb.append(ip);
        sb.append(":");
        sb.append(ssl ? https_port : http_port);
        return sb.toString();
    }

    @Override
    public void clean(String id) {
        NodeData nodeData = nodeDataService.getOneById(id);
        log.info("clean node [name:{},code:{}]", nodeData.getName(), nodeData.getAreaCode());
        if (remoteTokenManageService.contains(id)) {
            log.info("Sign out remote node ");
            String token = remoteTokenManageService.getToken(nodeData.getId());
            if (token != null) {
                if (WebsocketClient.getWebSocketMap().containsKey(token)) {
                    log.info("disconnect remote node websocket");
                    WebsocketClient.getWebSocketMap().remove(token).disconnect();
                }
            }
        }
    }

    @Override
    public Boolean openNodeWebsocketById(String id) {
        NodeData nodeData = nodeDataService.getOneById(id);
        String token = null;
        String ip = null;
        if(NodeType.THIS.value() ==  nodeData.getType()){
            if (SystemConfiguration.smcVersionIsV2()) {
                token = UUID.randomUUID().toString();
                SmcWebsocketClient.setToken(token);
                log.info("2.0token:{}",token);
            } else {
                token = localTokenManageService.getSmcToken(CoreConfig.INTERNAL_USER_TOKEN);
            }
            ip = "127.0.0.1";
        }else{
            token = remoteTokenManageService.getToken(id);
            ip = nodeData.getIp();
        }
        if(token == null){
            return false;
        }
        openNodeWebsocket(id, token, ip, nodeData.isHttps());
        return true;
    }

    @Override
    public void openNodeWebsocket(String id, String token, String ip, boolean ssl) {
        WebSocket webSocket = WebsocketClient.getWebSocketMap().get(id);
        if (webSocket != null) {
            webSocket.disconnect();
            WebsocketClient.getWebSocketMap().remove(id);
        }
        String port;
        if (ssl) {
            port = https_port;
        } else {
            port = http_port;
        }
        WebsocketClient.connect(ip, CoreApiUrl.GW_WEBSOCKET.value() + token, ssl, port, id);
    }

    @Override
    public GetNodeTokenResponse loginNode(String id, String userName, String password, String ip, boolean ssl, String areaCode) throws MyHttpException, Exception {
        String authorization = AuthorizationUtil.getAuthorization(userName, password);
        NodeData nodeData = nodeDataService.getLocal();
        GetNodeTokenRequest request = new GetNodeTokenRequest();
        request.setId(nodeData.getId());
        request.setName(nodeData.getName());
        request.setAreaCode(nodeData.getAreaCode());
        request.setSmcVersion(nodeData.getSmcVersion());
        log.info("setRealLocalGwId:{}", nodeData.toGwId());
        request.setRealLocalGwId(nodeData.toGwId());
        MultiValueMap<String, String> headers=new LinkedMultiValueMap<>();
        headers.set("Authorization",authorization);
        String httpResponse = httpService.post(urlSplice(ip, ssl) + CoreApiUrl.GET_NODE_TOKEN.value(), request,headers).getBody();
        GetNodeTokenResponse getTokenResponse = JSON.parseObject(httpResponse, GetNodeTokenResponse.class);
        return getTokenResponse;
    }
}

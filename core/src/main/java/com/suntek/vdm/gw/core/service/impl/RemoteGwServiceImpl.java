package com.suntek.vdm.gw.core.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.service.impl.HttpServiceImpl;
import com.suntek.vdm.gw.common.util.HttpUtil;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.pojo.RemoteToken;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.welink.dispenser.WelinkDispenser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 远端级联服务调用
 */
@Service
@Slf4j
public class RemoteGwServiceImpl extends HttpServiceImpl implements RemoteGwService {

    private String prefix = "";


    private ThreadLocal<String> REMOTE_GW_IP = new ThreadLocal<String>();
    private ThreadLocal<String> REMOTE_GW_TOKEN = new ThreadLocal<String>();
    private ThreadLocal<String> REAL_ID = new ThreadLocal<String>();
    private ThreadLocal<Boolean> FIRST = new ThreadLocal<Boolean>();
    private ThreadLocal<GwId> TARGET_ID = new ThreadLocal<GwId>();
    private ThreadLocal<Integer> TARGET_TYPE = new ThreadLocal<Integer>();
    private ThreadLocal<String> HEADER = new ThreadLocal<String>();


    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private RemoteTokenManageService remoteTokenManageService;
    @Autowired
    private LocalTokenManageService localTokenManageService;

    @Autowired
    @Qualifier("httpServiceImpl")
    private HttpService httpService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private WelinkDispenser welinkDispenser;

    @Autowired
    private NodeManageService nodeManageService;

    @Autowired
    private VmNodeTokenManagerService vmNodeTokenManagerService;
    @Autowired
    private VmNodeDataService vmNodeDataService;

    @Value("${cas.service.port}")
    private String https_port;

    @Value("${cas.service.http-port}")
    private String http_port;

    public String urlSplice(String ip, boolean ssl) {
        StringBuilder sb = new StringBuilder();
        sb.append(ssl ? "https://" : "http://");
        sb.append(ip);
        switch (HttpUtil.getIpType(ip)) {
            case "ipv4WithPort":
            case "ipv6WithPort":
            case "domainName":
                break;
            case "ipv6NoBracket": //需要拼接ipv6格式端口
                sb = new StringBuilder();
                sb.append(ssl ? "https://" : "http://");
                sb.append("[");
                sb.append(ip);
                sb.append("]");
            case "ipv6NoPort": //需要拼接端口
            case "ipv4NoPort": //需要拼接端口
            case "otherType":
                //拼接端口
                sb.append(":");
                sb.append(ssl ? https_port : http_port);
                break;
        }
        return sb.toString();
    }




    @Override
    public RemoteGwServiceImpl toTop() throws MyHttpException {
        NodeData top = nodeDataService.getTop();
        if (top != null) {
            toByGwId(top.toGwId());
        } else {
            log.debug("top is null");
        }
        return this;
    }

    @Override
    public RemoteGwServiceImpl toByGwId(GwId id) throws MyHttpException {
        FIRST.set(true);
        if (id.inComplete()) {
            GwId completeGwId = routManageService.getCompleteGwIdBy(id);
            if (completeGwId != null) {
                id = completeGwId;
            } else {
                throw new MyHttpException(HttpStatus.NOT_ACCEPTABLE.value(), "smc node not found.");
            }
        }
        TARGET_ID.set(id);
        GwId realId = null;
        boolean isLocal = false;
        NodeData localNodeData = nodeDataService.getLocal();
        if(localNodeData.toGwId().equals(id)){
            realId = id;
            isLocal = true;
        }else {
            realId = routManageService.getWayByGwId(id);
        }
        if (realId == null) {
            NodeData topNodeData = nodeDataService.getTop();
            if (topNodeData == null) {
                throw new MyHttpException(HttpStatus.NOT_ACCEPTABLE.value(), "smc node not found.");
            } else {
                realId = topNodeData.toGwId();
            }
        }
        NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(realId.getNodeId());
        if(nodeBusinessType != null){
            TARGET_TYPE.set(nodeBusinessType.value());
        }else {
            TARGET_TYPE.set(NodeBusinessType.SMC.value());//默认都是SMC
        }

        REAL_ID.set(realId.getNodeId());
        RemoteToken remoteToken = null;
        //        转发问题
        Boolean isVm = false;
        GwNode gwNode = nodeManageService.getGwNodeById(realId.getNodeId());
        if(gwNode != null && gwNode.getIsVmNode() != null && gwNode.getIsVmNode()){
//            本级虚拟节点
            if (realId.getNodeId().equals(id.getNodeId())) {
                HEADER.set("localVm");
                TARGET_ID.set(localNodeData.toGwId());
            }
            isVm = true;
        }
        if(isLocal){
            HEADER.set("localVm");
            LocalToken localToken = localTokenManageService.get(CoreConfig.INTERNAL_USER_TOKEN);
            remoteToken = new RemoteToken(realId.getNodeId(), localToken.getSmcToken(), "127.0.0.1", localNodeData.isHttps(), localNodeData.getAreaCode(), localToken.getExpire());
        }else if(isVm){
            remoteToken = vmNodeTokenManagerService.get(id.getNodeId());
        }else{
            remoteToken = remoteTokenManageService.get(realId.getNodeId());
        }
        if (remoteToken == null || remoteToken.isExpire()) {
            NodeData nodeData = nodeDataService.getOneById(realId.getNodeId());
            boolean result = false;
            if(nodeData == null){
                VmNodeData vmNodeData = vmNodeDataService.getOneById(realId.getNodeId());
                result = vmNodeTokenManagerService.triggerKeepAlive(vmNodeData);
                remoteToken = vmNodeTokenManagerService.get(vmNodeData.getId());
            }else{
                result = remoteTokenManageService.triggerKeepAlive(nodeData);
                remoteToken = remoteTokenManageService.get(nodeData.getId());
            }
            if(!result){
                throw new MyHttpException(HttpStatus.UNAUTHORIZED.value(), "GW node not login or GW Server offline.");
            }


//            NodeData nodeData = nodeDataService.getOneById(realId.getNodeId());
//            String nodeId = nodeManageService.loginNode(nodeData);
//            if (nodeId == null) {
//                throw new MyHttpException(HttpStatus.NOT_ACCEPTABLE.value(), "smc node login fail.");
//            }
        }
        REMOTE_GW_TOKEN.set(remoteToken.getToken());
        REMOTE_GW_IP.set(urlSplice(remoteToken.getIp(), remoteToken.isSsl()));
        return this;
    }


    @Override
    public ResponseEntity<String> request(String url, Object body, HttpMethod method) throws MyHttpException {
        return this.request(url, body, null, method);
    }

    @Override
    public ResponseEntity<String> request(String uri, Object body, MultiValueMap<String, String> headers, HttpMethod method) throws MyHttpException {
        String url = "";
        try {
            if(!uri.contains("http")){
                url = REMOTE_GW_IP.get() + prefix + uri;
            }else{
                url = uri;
            }
            if (headers == null) {
                headers = new LinkedMultiValueMap<String, String>();
            }
            String useOrgUser = headers.getFirst("useOrgUser");
            if(StringUtils.isEmpty(useOrgUser) || !"true".equals(useOrgUser) || StringUtils.isEmpty(headers.getFirst("Token"))){
                headers.set("Token", REMOTE_GW_TOKEN.get());
            }
            if (TARGET_ID.get() != null) {
                headers.set("TargetId", TARGET_ID.get().toString());
            }
            if (HEADER.get() != null) {
                headers.set("localVm", "true");
            }else{
                headers.remove("localVm");
            }
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes != null) {
                HttpServletRequest request = servletRequestAttributes.getRequest();
                String gwHttpCode = request.getHeader("gw-http-code");
                if (gwHttpCode != null) {
                    headers.set("gw-http-code", gwHttpCode);
                } else {
                    headers.set("gw-http-code", String.valueOf(System.currentTimeMillis()));
                }
            } else {
                headers.set("gw-http-code", String.valueOf(System.currentTimeMillis()));
            }
            if (body == null) {
                body = new LinkedMultiValueMap<>();
            }
            if(NodeBusinessType.WELINK.value() == TARGET_TYPE.get() || NodeBusinessType.CLOUDLINK.value() == TARGET_TYPE.get()){
                return welinkDispenser.WelinkDispenser(url, body, headers, method);
            }
            return request(url, body, headers, method, MediaType.APPLICATION_JSON_UTF8, "S", "GW");
        } catch (MyHttpException e) {
            if (e.getCode() == 401) {
                throw new MyHttpException(HttpStatus.UNAUTHORIZED.value(), "GW node not login or GW Server offline.");
//                if (FIRST.get()) {
//                    //重新登录一次
//                    NodeData nodeData = nodeDataService.getOneById(REAL_ID.get());
//                    String nodeId = nodeManageService.loginNode(nodeData);
//                    if (nodeId == null) {
//                        throw new MyHttpException(HttpStatus.NOT_ACCEPTABLE.value(), "smc node login fail.");
//                    }
//                    RemoteToken remoteToken = remoteTokenManageService.get(nodeId);
//                    REMOTE_GW_TOKEN.set(remoteToken.getToken());
//                    FIRST.set(false);
//                    return request(url, body, headers, method);
//                }
            }
            throw e;
        } finally {
            //自定义的线程变量  必须在使用结束的时候进行回收 不清理可能会被复用
            REMOTE_GW_IP.remove();
            REMOTE_GW_TOKEN.remove();
            REAL_ID.remove();
            FIRST.remove();
            TARGET_ID.remove();
            TARGET_TYPE.remove();
            HEADER.remove();
        }
    }
}

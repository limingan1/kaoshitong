package com.suntek.vdm.gw.core.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.CoreApiUrl;
import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.common.pojo.WarningType;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.service.WarningReportService;
import com.suntek.vdm.gw.common.util.AuthorizationUtil;
import com.suntek.vdm.gw.common.util.HttpUtil;
import com.suntek.vdm.gw.common.util.ReflectUtil;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.core.api.request.node.*;
import com.suntek.vdm.gw.core.api.response.node.GetNodeTokenResponse;
import com.suntek.vdm.gw.core.api.response.node.GetRemoteInfoResponse;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.pojo.RemoteToken;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.core.service.orgUser.OrgUserConfigService;
import com.suntek.vdm.gw.core.service.orgUser.OrgUserDataService;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import com.suntek.vdm.gw.welink.api.response.AuthResponse;
import com.suntek.vdm.gw.welink.service.WeLinkLinkService;
import com.suntek.vdm.gw.welink.service.WelinkAddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class NodeConfigServiceImpl implements NodeConfigService {
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private SmcLoginService smcLoginService;
    @Autowired
    private WeLinkLinkService weLinkLinkService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private AsyncService asyncService;
    @Autowired
    private UserService userService;
    @Autowired
    @Qualifier("httpServiceImpl")
    private HttpService httpService;

    @Autowired
    private WarningReportService warningReportService;
    @Autowired
    private WelinkAddressBookService welinkAddressBookService;
    @Autowired
    private RemoteTokenManageService remoteTokenManageService;
    @Autowired
    private OrgUserConfigService orgUserConfigService;

    @Override
    public GetNodeTokenResponse getNodeTokens(GetNodeTokenRequest request, String ip, String authorization) throws BaseStateException {
        GetNodeTokenResponse getNodeTokenResponse = new GetNodeTokenResponse();
        GetTokenResponse getTokenResponse = userService.getTokens(authorization, false, request.getRealLocalGwId());
        localTokenManageService.setCode(getTokenResponse.getUuid(), request.getId());
        NodeData nodeData = nodeDataService.getLocal();
        getNodeTokenResponse.setId(nodeData.getId());
        getNodeTokenResponse.setName(nodeData.getName());
        getNodeTokenResponse.setAreaCode(nodeData.getAreaCode());
        getNodeTokenResponse.setSmcVersion(nodeData.getSmcVersion());
        getNodeTokenResponse.setUuid(getTokenResponse.getUuid());
        getNodeTokenResponse.setExpire(getTokenResponse.getExpire());
        asyncService.getNodeTokensAfter(ip, request);
        return getNodeTokenResponse;
    }

    @Override
    public void add(AddNodeRequest request) throws MyHttpException {
        if (NodeBusinessType.WELINK.value() != request.getBusinessType() || NodeType.LOW.value() != request.getType()) {
            request.setPermissionSwitch(null);
        }
        log.info("add node({})", request.toString());
        AddNodeVerifyRequest addNodeVerifyRequest = new AddNodeVerifyRequest();
        BeanUtils.copyProperties(request, addNodeVerifyRequest);
        addNodeVerify(addNodeVerifyRequest);
        NodeData nodeData = new NodeData();
        ReflectUtil.copyPropertiesSimple(request, nodeData);
        nodeData.setBusinessType(request.getBusinessType());
        boolean isLocal = request.getType().equals(NodeType.THIS.value());
        //如果不是本级
        if (!isLocal) {
            NodeBusinessType nodeBusinessType = NodeBusinessType.valueOf(request.getBusinessType());
            //如果是SMC
            if (nodeBusinessType.equals(NodeBusinessType.SMC)) {
                String smcIp = request.getIp();
                switch (HttpUtil.getIpType(smcIp)) {
                    case "ipv4WithPort":
                        smcIp = smcIp.split(":")[0];
                        break;
                    case "ipv6WithPort":
                        Pattern compile = Pattern.compile("\\[[a-zA-Z0-9:%]+\\]");
                        Matcher matcher = compile.matcher(smcIp);
                        if (matcher.find()) {
                            String groupIp = matcher.group(0);
                            smcIp = groupIp.substring(1, groupIp.length() - 1);//截取掉ipv6格式两边的括号
                        }
                        break;
                }
                GetRemoteInfoResponse getRemoteInfoResponse = getRemoteInfo(request.getUsername(), request.getPassword(), request.getIp(), request.getSsl() == 1);
                nodeData.setIp(smcIp);
                nodeData.setId(getRemoteInfoResponse.getRemoteId());
                nodeData.setName(getRemoteInfoResponse.getRemoteName());
                nodeData.setAreaCode(getRemoteInfoResponse.getRemoteCode());
            } else {
                nodeData.setClientSecret(request.getClientSecret());
                nodeData.setClientId(request.getClientId());
                nodeData.setAddressBookUrl(request.getAddressBookUrl());
            }
            nodeData.setPermissionSwitch(request.getPermissionSwitch());
        } else {
            nodeData.setIp(SystemConfiguration.getSmcAddress());
        }
        BaseState baseState = null;
        try {
            baseState = nodeDataService.add(nodeData);
        } catch (Exception e) {
            throw new MyHttpException(409, e.getMessage());
        }
        if (baseState.getCode() == 1) {
            log.info("node add success");
            //节点添加成功后
            nodeManageService.add(nodeData.getId());
            localTokenManageService.delByNodeId(nodeData.getId());
            if (isLocal) {
                warningReportService.deleteWarningReport(WarningType.LOCAL_NODE_NOT_CONFIG);
            }
        } else {
            log.info("node add fail,error:{}", baseState.getMsg());
            throw new MyHttpException(409, baseState.getMsg());
        }
    }

    @Override
    public void update(UpdateNodeRequest request) throws MyHttpException {
        log.info("update node({})", request.toString());
        NodeData old = nodeDataService.getOneById(request.getId());
        if (StringUtils.isEmpty(request.getPassword())) {
            request.setPassword(old.decryptPassword());
        }
        if (NodeBusinessType.isWelinkOrCloudLink(old.getBusinessType()) || NodeType.LOW.value() != old.getType()) {
            request.setPermissionSwitch(null);
        }
        UpdateNodeVerifyRequest checkUpdateNodeRequest = new UpdateNodeVerifyRequest();
        BeanUtils.copyProperties(request, checkUpdateNodeRequest);
        updateNodeVerify(checkUpdateNodeRequest);
        if (old.getType().equals(NodeType.THIS.value())) {
            old.setUsername(request.getUsername());
            old.setPassword(request.getPassword());
            old.setName(request.getName());
            old.setAreaCode(request.getAreaCode());
            old.setIp(SystemConfiguration.getSmcAddress());
            BaseState baseState = nodeDataService.update(old);
            if (baseState.getCode() == 1) {
                nodeManageService.update(old.getId());
                log.info("node update success");
            } else {
                log.info("node update fail,error{}", baseState.getMsg());
                throw new MyHttpException(409, baseState.getMsg());
            }
        } else {
            old.setUsername(request.getUsername());
            old.setPassword(request.getPassword());
            if (NodeBusinessType.SMC.value() == old.getBusinessType()) {
                GetRemoteInfoResponse getRemoteInfoResponse = getRemoteInfo(request.getUsername(), request.getPassword(), request.getIp(), request.getSsl() == 1);
                old.setName(getRemoteInfoResponse.getRemoteName());
                old.setAreaCode(getRemoteInfoResponse.getRemoteCode());
            }else{
                //允许修改下级welink节点名称
                old.setName(request.getName());
                old.setAreaCode(request.getAreaCode());
                old.setClientSecret(request.getClientSecret());
                old.setClientId(request.getClientId());
                old.setAddressBookUrl(request.getAddressBookUrl());
            }
            old.setVmrConfId(request.getVmrConfId());
            old.setPermissionSwitch(request.getPermissionSwitch());
            BaseState baseState = nodeDataService.update(old);
            if (baseState.getCode() == 1) {
                nodeManageService.update(old.getId());
                log.info("node update success");
            } else {
                log.info("node update fail,error{}", baseState.getMsg());
                throw new MyHttpException(409, baseState.getMsg());
            }
        }
    }

    @Override
    public void del(String id) throws MyHttpException {
        NodeData old = new NodeData();
        BeanUtils.copyProperties(nodeDataService.getOneById(id), old);
        log.info("del node(id:{} name:{},code:{})", old.getId(), old.getName(), old.getAreaCode());
        NodeType nodeType = NodeType.valueOf(old.getType());
        String name = old.getName();
        nodeManageService.del(old.getId(), name, nodeType);
        localTokenManageService.delByNodeId(old.getId());
        BaseState baseState = nodeDataService.del(id);
        if (baseState.getCode() == 1) {
            nodeManageService.autoPush();
            if (NodeBusinessType.isWelinkOrCloudLink(old.getBusinessType())) {
                welinkAddressBookService.removeAccessToken();
            }
            if (old.getType() != 0) {
                //若删除的不是本级，需要删除上下级配置的分职
                orgUserConfigService.delByNodeId(old.getId());
            }
            log.info("del node success");
        } else {
            log.info("del update fail,error{}", baseState.getMsg());
            throw new MyHttpException(409, baseState.getMsg());
        }
    }


    public GetRemoteInfoResponse getRemoteInfo(String username, String password, String ip, boolean ssl) throws MyHttpException {
        String authorization = AuthorizationUtil.getAuthorization(username, password);
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.set("Authorization", authorization);
        try {
            String httpResponse = httpService.get(remoteGwService.urlSplice(ip, ssl) + CoreApiUrl.NODE_REMOTE_INFO.value(), null, headers).getBody();
            GetRemoteInfoResponse getRemoteInfoResponse = JSON.parseObject(httpResponse, GetRemoteInfoResponse.class);
            if (getRemoteInfoResponse.getCode() != 1) {
                log.info("Get remote node info error：{}", getRemoteInfoResponse.getMsg());
                throw new MyHttpException(409, getRemoteInfoResponse.getMsg());
            } else {
                return getRemoteInfoResponse;
            }
        } catch (MyHttpException e) {
            log.info("Get remote node info network error：{}", e.toString());
            String body = e.getBody();
            if (e.getBody().contains(" : ")) {
                body = e.getBody().split(" : ")[1];
                body = body.substring(1, body.length() - 1);
            }
            log.info("e.body1:{}", body);
            throw new MyHttpException(e.getCode(), body);
        }
    }

    @Override
    public GetTokenResponse loginSmc(LoginSmcRequest request) throws MyHttpException {
//        SmcHttpServiceImpl.ip = request.getIpAddress();
        String authorization = AuthorizationUtil.getAuthorization(request.getUsername(), request.getPassword(), false);
        try {
            GetTokenResponse res = userService.getTokens(authorization, false, null);
            if (res == null || res.getUuid() == null) {
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
            }
            LocalToken localToken = localTokenManageService.get(res.getUuid());
            localToken.setExpire(localToken.getExpire()+3*60*1000);

//            redisTemplate.opsForValue().set(res.getUuid(), "2", 30, TimeUnit.MINUTES);
            return res;
        } catch (BaseStateException e) {
            log.error("error: {} {}", e.getMessage(), e.toString());
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
        }
    }


    @Override
    public void addNodeVerify(AddNodeVerifyRequest request) throws MyHttpException {
        NodeType nodeType = NodeType.valueOf(request.getType());
        String authorization = AuthorizationUtil.getAuthorization(request.getUsername(), request.getPassword(), false);
        switch (nodeType) {
            case THIS:
                if (nodeDataService.getLocal() != null) {
                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NODE_EXISTS.toString());
                }
                if (nodeDataService.any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getName, request.getName()))) {
                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NAME_EXISTS.toString());
                }
                if (nodeDataService.any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getAreaCode, request.getAreaCode()))) {
                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_AREA_CODE_EXISTS.toString());
                }
                try {
                    GetTokenResponse response = smcLoginService.getTokens(authorization);
                    if (response == null || response.getUuid() == null) {
                        throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
                    }
                } catch (MyHttpException e) {
                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
                }
                break;
            case TOP:
            case LOW: {
                NodeBusinessType nodeBusinessType = NodeBusinessType.valueOf(request.getBusinessType());
                switch (nodeBusinessType) {
                    case SMC: {
                        RemoteAddNodeVerifyRequest remoteAddNodeVerifyRequest = new RemoteAddNodeVerifyRequest();
                        if (request.getType().equals(NodeType.LOW.value())) {
                            //检测下级IP是否存在 存在认为已经配置了下级
                            if (nodeDataService.any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getIp, request.getIp()))) {
                                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NODE_EXISTS.toString());
                            }
                            remoteAddNodeVerifyRequest.setFormType(NodeType.TOP.value());
                        } else {
                            if (nodeDataService.getTop() != null) {
                                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NODE_EXISTS.toString());
                            }
                            remoteAddNodeVerifyRequest.setFormType(NodeType.LOW.value());
                        }
                        NodeData nodeDataLocal = nodeDataService.getLocal();
                        remoteAddNodeVerifyRequest.setName(nodeDataLocal.getName());
                        remoteAddNodeVerifyRequest.setAreaCode(nodeDataLocal.getAreaCode());
                        remoteAddNodeVerifyRequest.setId(nodeDataLocal.getId());
                        try {
                            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                            AuthorizationUtil.setAuthorization(authorization, headers);
                            String httpResponse = httpService.post(remoteGwService.urlSplice(request.getIp(), request.getSsl() == 1) + CoreApiUrl.CHECK_NODE_ADD_REMOTE.value(), remoteAddNodeVerifyRequest, headers).getBody();
                        } catch (MyHttpException e) {
                            throwException(e);
                        }
                        break;
                    }
                    case WELINK: {
                        //welink只能配置为下级
                        if (request.getType().equals(NodeType.LOW.value())) {
                            //检测下级IP是否存在 存在认为已经配置了下级
                            if (nodeDataService.any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getIp, request.getIp()))) {
                                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NODE_EXISTS.toString());
                            }
                            try {
                                AuthResponse authResponse = weLinkLinkService.login(request.getIp(), request.getUsername(), request.getPassword());
                                welinkAddressBookService.getTickets(request.getClientId(), request.getClientSecret(), request.getAddressBookUrl(), true);
                            } catch (MyHttpException e) {
                                if (e.getCode() == 401) {
                                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
                                } else if (e.getCode() == 404) {
                                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
                                } else if (e.getCode() == 500) {
                                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NETWORK_ERROR.toString());
                                } else if (e.getCode() == 423){
                                    throw new MyHttpException(409, GwErrorCode.ACCOUNT_LOCKED.toString());
                                } else {
                                    throw new MyHttpException(409, e.getBody());
                                }
                            }
                        }
                        break;
                    }
                    case CLOUDLINK: {
                        if (request.getType().equals(NodeType.LOW.value())) {
                            //检测下级IP是否存在 存在认为已经配置了下级
                            if (nodeDataService.any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getIp, request.getIp()))) {
                                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NODE_EXISTS.toString());
                            }
                            try {
                                AuthResponse authResponse = weLinkLinkService.cloudLinkLogin(request.getIp(), request.getUsername(), request.getPassword());
//                                welinkAddressBookService.getTickets(request.getClientId(), request.getClientSecret(), request.getAddressBookUrl(), true);
                            } catch (MyHttpException e) {
                                if (e.getCode() == 401) {
                                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
                                } else if (e.getCode() == 404) {
                                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
                                } else if (e.getCode() == 500) {
                                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NETWORK_ERROR.toString());
                                } else {
                                    throw new MyHttpException(409, e.getBody());
                                }
                            }
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
            default:
                break;
        }
    }

    private void throwException(MyHttpException e) throws MyHttpException{
        if (e == null) {
            return;
        }
        switch (e.getCode()) {
            case 500:
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
            case 404:
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NETWORK_ERROR.toString());
            default:
                throw e;
        }
    }


    @Override
    public void remoteAddNodeVerify(RemoteAddNodeVerifyRequest request, String authorization) throws MyHttpException {
//        if (request.getFormType() == SmcNodeType.LOW.value()) {
//            //来自下级 将本级配置为上级 本地必须配置下级
//            VdmSmcNode vdmSmcNode = vdmSmcNodeService.getOneById(request.getId());
//            if (vdmSmcNode == null) {
//                throw new BaseStateException("远端没有把本级配置为下级");
//            }
//        } else
        if (request.getFormType() == NodeType.TOP.value()) {
            //来自上级 检测本地有没有上级  有的话拒绝
            NodeData top = nodeDataService.getTop();
            if (nodeDataService.getTop() != null && !top.getId().equals(request.getId()) && !top.getAreaCode().equals(request.getAreaCode())) {
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_REMOTE_HAS_TOP.toString());
            }
        }
        if (nodeDataService.any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getName, request.getName()).ne(NodeData::getType, request.getFormType()))) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_REMOTE_NAME_EXISTS.toString());
        }
        if (nodeDataService.any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getAreaCode, request.getAreaCode()).ne(NodeData::getType, request.getFormType()))) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_REMOTE_AREA_CODE_EXISTS.toString());
        }
        try {
            smcLoginService.getTokens(authorization);
        } catch (MyHttpException e) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
        }
    }

    @Override
    public void updateNodeVerify(UpdateNodeVerifyRequest request) throws MyHttpException {
        NodeData old = nodeDataService.getOneById(request.getId());
        if (StringUtils.isEmpty(request.getPassword())) {
            request.setPassword(old.decryptPassword());
        }
        String authorization = AuthorizationUtil.getAuthorization(request.getUsername(), request.getPassword(), false);
        if (old.getType().equals(NodeType.THIS.value())) {
            if (!request.getName().equals(old.getName()) && nodeDataService.any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getName, request.getName()))) {
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NAME_EXISTS.toString());
            }
            if (!request.getAreaCode().equals(old.getAreaCode()) && nodeDataService.any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getAreaCode, request.getAreaCode()))) {
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_AREA_CODE_EXISTS.toString());
            }
            if (equalsAccount(request.getUsername(), request.getPassword(), old)) {
                return;
            }
            try {
                smcLoginService.getTokens(authorization);
            } catch (MyHttpException e) {
                if (e.getCode() == 401) {
                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
                } else if (e.getCode() == 404) {
                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
                } else {
                    throw e;
                }
            }
        } else {
            try {
                MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                AuthorizationUtil.setAuthorization(request.getUsername(), request.getPassword(), headers);
                Integer type = old.getBusinessType();
                if (NodeBusinessType.WELINK.value() == type) {
                    //welink
                    AuthResponse authResponse = weLinkLinkService.login(request.getIp(), request.getUsername(), request.getPassword());
                    welinkAddressBookService.getTickets(request.getClientId(), request.getClientSecret(),request.getAddressBookUrl(),true);
                } else if(NodeBusinessType.CLOUDLINK.value() == type){
                    AuthResponse authResponse = weLinkLinkService.cloudLinkLogin(request.getIp(), request.getUsername(), request.getPassword());
                    welinkAddressBookService.getTickets(request.getClientId(), request.getClientSecret(),request.getAddressBookUrl(),true);
                } else{
                    if (equalsAccount(request.getUsername(), request.getPassword(), old)) {
                        return;
                    }
                    String httpResponse = httpService.post(remoteGwService.urlSplice(request.getIp(), request.getSsl() == 1) + CoreApiUrl.CHECK_NODE_UPDATE_REMOTE.value(), null, headers).getBody();
                }
            } catch (MyHttpException e) {
                if (e.getCode() == 401) {
                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
                } else if (e.getCode() == 500) {
                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NETWORK_ERROR.toString());
                } else if (e.getCode() == 404) {
                    throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NETWORK_ERROR.toString());
                } else if (e.getCode() == 423){
                    throw new MyHttpException(409, GwErrorCode.ACCOUNT_LOCKED.toString());
                } else {
                    throw new MyHttpException(409, e.getBody());
                }
            }
        }
    }

    private boolean equalsAccount(String username, String password, NodeData nodeData) {
        if (nodeData.getUsername().equals(username) && nodeData.getPassword().equals(password)) {
            if (NodeType.isRemoteNode(nodeData.getType())) {
                RemoteToken remoteToken = remoteTokenManageService.get(nodeData.getId());
                return remoteToken != null && remoteToken.getToken() != null;
            }
            return true;
        }
        return false;
    }

    @Override
    public void remoteUpdateNodeVerify(String authorization) throws MyHttpException {
        try {
            smcLoginService.getTokens(authorization);
        } catch (MyHttpException e) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
        }
    }


    @Override
    public GetRemoteInfoResponse getRemoteInfoHandler(String authorization) throws MyHttpException {
        try {
            smcLoginService.getTokens(authorization);
        } catch (MyHttpException e) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
        }
        GetRemoteInfoResponse response = new GetRemoteInfoResponse();
        response.setCode(1);
        response.setMsg("");
        NodeData nodeData = nodeDataService.getLocal();
        if(nodeData == null){
            throw new MyHttpException(409,GwErrorCode.NO_LOCAL_NODE_ERROR.toString());
        }
        response.setRemoteId(nodeData.getId());//回复本级的ID
        response.setRemoteName(nodeData.getName());//回复本级的名称
        response.setRemoteCode(nodeData.getAreaCode());//回复本级的地区编码
        return response;
    }


    /**
     * 远端节点信息变更处理
     *
     * @param request
     * @throws BaseStateException
     */
    @Override
    public void remoteNodeUpdateHandler(RemoteNodeUpdateRequest request) {
        log.info("remote node update handler [{}]", request.toString());
        NodeType nodeType = NodeType.valueOf(request.getFormType());
        NodeData nodeData = nodeDataService.getOneById(request.getId());
        if (nodeData != null) {
            nodeData.setAreaCode(request.getAreaCode());
            nodeData.setName(request.getName());
            nodeData.setSmcVersion(request.getSmcVersion());
            nodeData.setPassword(nodeData.decryptPassword());//解密密码
            BaseState baseState = nodeDataService.update(nodeData);
        }
    }
}

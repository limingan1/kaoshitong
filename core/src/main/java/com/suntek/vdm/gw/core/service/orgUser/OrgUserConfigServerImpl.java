package com.suntek.vdm.gw.core.service.orgUser;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.util.AuthorizationUtil;
import com.suntek.vdm.gw.common.util.ReflectUtil;
import com.suntek.vdm.gw.core.api.request.orguser.AddOrgUserRequest;
import com.suntek.vdm.gw.core.api.request.orguser.OrgUserDetailsResponse;
import com.suntek.vdm.gw.core.api.request.orguser.UpdateOrgUserRequest;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.entity.OrgUserData;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class OrgUserConfigServerImpl implements OrgUserConfigService {
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private OrgUserDataService orUsergDataService;

    @Autowired
    private SmcLoginService smcLoginService;
    @Autowired
    private OrgUserManagerService orgUserManagerService;
    @Autowired
    private VmNodeDataService vmNodeDataService;

    @Qualifier("httpServiceImpl")
    @Autowired
    private HttpService httpService;

    @Value("${cas.service.port}")
    private String https_port;

    @Value("${cas.service.http-port}")
    private String http_port;

    public String urlSplice(String ip, boolean ssl) {
        StringBuilder sb = new StringBuilder();
        sb.append(ssl ? "https://" : "http://");
        sb.append(ip);
        sb.append(":");
        sb.append(ssl ? https_port : http_port);
        return sb.toString();
    }

    @Override
    public void add(AddOrgUserRequest request) throws MyHttpException {
        log.info("add vm node({})", request.toString());
        addNodeVerify(request);

        OrgUserData orgUserData = new OrgUserData();
        ReflectUtil.copyPropertiesSimple(request, orgUserData);
        BaseState baseState = null;
        try {
            baseState = orUsergDataService.add(orgUserData);
        } catch (Exception e) {
            throw new MyHttpException(409, e.getMessage());
        }
        if (baseState.getCode() == 1) {
            log.info("node add success");
            //登录
            orgUserManagerService.add(orgUserData.getId());
        } else {
            log.info("node add fail,error:{}", baseState.getMsg());
            throw new MyHttpException(409, baseState.getMsg());
        }
    }

    @Override
    public void addNodeVerify(AddOrgUserRequest request) throws MyHttpException {
        //添加了虚拟节点，无法配置分职
        if (vmNodeDataService.hasVmNode()) {
            throw new MyHttpException(409, GwErrorCode.CANT_CONFIGURED_VM_NODE_ORG.toString());
        }
        String authorization = AuthorizationUtil.getAuthorization(request.getUsername(), request.getPassword(), false);
        NodeData nodeData = nodeDataService.getOneById(request.getNodeId());
        if(nodeData == null){
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NOT_EXISTS.toString());
        }
        if (orUsergDataService.any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getName, request.getName()).eq(OrgUserData::getNodeId, request.getNodeId()))) {
            throw new MyHttpException(409, GwErrorCode.NAME_EXISTS.toString());
        }
        if(orUsergDataService.any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getOrgId, request.getOrgId()).eq(OrgUserData::getNodeId, request.getNodeId()))){
            throw new MyHttpException(409, GwErrorCode.ORGID_EXISTS.toString());
        }
        if(orUsergDataService.any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getUsername, request.getUsername()).eq(OrgUserData::getNodeId, request.getNodeId()))){
            throw new MyHttpException(409, GwErrorCode.USERNAME_EXISTS.toString());
        }
        try {
            GetTokenResponse getTokenResponse = orgUserManagerService.loginRemote(nodeData.getId(), authorization);
            if (getTokenResponse == null || getTokenResponse.getUuid() == null) {
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
            }
        } catch (MyHttpException e) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
        }

    }

    @Override
    public void update(UpdateOrgUserRequest request) throws MyHttpException {
        log.info("update vm node({})", request.toString());
        OrgUserData old = orUsergDataService.getOneById(request.getId());
        if (StringUtils.isEmpty(request.getPassword()) || "******".equals(request.getPassword())) {
            request.setPassword(old.decryptPassword());
        }
        updateNodeVerify(request);
        old.setUsername(request.getUsername());
        old.setPassword(request.getPassword());
        old.setName(request.getName());
        old.setOrgId(request.getOrgId());
        BaseState baseState = orUsergDataService.update(old);
        if (baseState.getCode() == 1) {
            log.info("vm node update success");
            orgUserManagerService.update(old.getId());
        } else {
            log.info("vm node update fail,error{}", baseState.getMsg());
            throw new MyHttpException(409, baseState.getMsg());
        }
    }




    public void updateNodeVerify(UpdateOrgUserRequest request) throws MyHttpException {
        OrgUserData old = orUsergDataService.getOneById(request.getId());
        if (StringUtils.isEmpty(request.getPassword()) || "******".equals(request.getPassword())) {
            request.setPassword(old.decryptPassword());
        }
        NodeData nodeData = nodeDataService.getOneById(request.getNodeId());
        if(nodeData == null){
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NOT_EXISTS.toString());
        }
        if (orUsergDataService.any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getName, request.getName()).ne(OrgUserData::getId, request.getId()).eq(OrgUserData::getNodeId, request.getNodeId()))) {
            throw new MyHttpException(409, GwErrorCode.NAME_EXISTS.toString());
        }
        if(orUsergDataService.any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getOrgId, request.getOrgId()).ne(OrgUserData::getId, request.getId()).eq(OrgUserData::getNodeId, request.getNodeId()))){
            throw new MyHttpException(409, GwErrorCode.ORGID_EXISTS.toString());
        }
        if(orUsergDataService.any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getUsername, request.getUsername()).ne(OrgUserData::getId, request.getId()).eq(OrgUserData::getNodeId, request.getNodeId()))){
            throw new MyHttpException(409, GwErrorCode.USERNAME_EXISTS.toString());
        }
        String authorization = AuthorizationUtil.getAuthorization(request.getUsername(), request.getPassword(), false);

        try {
            GetTokenResponse getTokenResponse = orgUserManagerService.loginRemote(nodeData.getId(), authorization);
            if (getTokenResponse == null || getTokenResponse.getUuid() == null) {
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
            }
        } catch (MyHttpException e) {
            if (e.getCode() == 401) {
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
            } else {
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
            }
        }

    }



    @Override
    public void del(String id) throws MyHttpException {
        if (id == null || !orUsergDataService.any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getId, id))) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NOT_EXISTS.toString());
        }
        OrgUserData old = new OrgUserData();
        BeanUtils.copyProperties(orUsergDataService.getOneById(id), old);
        log.info("del node(id:{} name:{},orgId:{})", old.getId(), old.getName(), old.getOrgId());
//        nodeManageService.del(old.getId(), old.getName(), NodeType.LOW);
        orgUserManagerService.del(old.getId());
        BaseState baseState = orUsergDataService.del(id);
        if (baseState.getCode() == 1) {
            log.info("del node success");
        } else {
            log.info("del update fail,error{}", baseState.getMsg());
            throw new MyHttpException(409, baseState.getMsg());
        }
    }

    @Override
    public OrgUserDetailsResponse getOneById(String id) {
        OrgUserDetailsResponse orgUserDetailsResponse = new OrgUserDetailsResponse();
        OrgUserData orgUserData = orUsergDataService.getOneById(id);
        if (orgUserData != null){
            BeanUtils.copyProperties(orgUserData, orgUserDetailsResponse);
        }
        return orgUserDetailsResponse;
    }

    @Override
    public List<OrgUserDetailsResponse> list(String nodeId) {
        List<OrgUserData> list = orUsergDataService.getListByLambda(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getNodeId, nodeId).orderByAsc(OrgUserData::getName));
        List<OrgUserDetailsResponse> respList = new ArrayList<>();
        for(OrgUserData orgUserData: list){
            OrgUserDetailsResponse orgUserDetailsResponse = new OrgUserDetailsResponse();
            BeanUtils.copyProperties(orgUserData, orgUserDetailsResponse);
            respList.add(orgUserDetailsResponse);
        }
        return respList;
    }

    @Override
    public boolean hasOrgUser() {
        return orUsergDataService.count(new LambdaQueryWrapper<>()) > 0;
    }

    @Override
    public void delByNodeId(String nodeId){
        if (nodeId == null ) {
            return;
        }
        List<OrgUserData> list = orUsergDataService.getListByLambda(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getNodeId, nodeId));
        if(list == null || list.isEmpty()){
            return;
        }
        for(OrgUserData orgUserData : list){
            log.info("del node(id:{} name:{},orgId:{})", orgUserData.getId(), orgUserData.getName(), orgUserData.getOrgId());
            orgUserManagerService.del(orgUserData.getId());
            orUsergDataService.del(orgUserData.getId());
        }
    }
}

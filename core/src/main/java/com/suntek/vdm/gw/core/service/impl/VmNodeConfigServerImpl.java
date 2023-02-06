package com.suntek.vdm.gw.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.common.util.AuthorizationUtil;
import com.suntek.vdm.gw.common.util.ReflectUtil;
import com.suntek.vdm.gw.core.api.request.vm.AddVmNodeRequest;
import com.suntek.vdm.gw.core.api.request.vm.UpdateVmNodeRequest;
import com.suntek.vdm.gw.core.api.response.vm.VmDetailsResponse;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.VmNodeConfigService;
import com.suntek.vdm.gw.core.service.VmNodeDataService;
import com.suntek.vdm.gw.core.service.orgUser.OrgUserConfigService;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;

@Service
@Slf4j
public class VmNodeConfigServerImpl implements VmNodeConfigService {

    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private SmcLoginService smcLoginService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private VmNodeTokenManagerServiceImpl vmNodeTokenManagerService;
    @Autowired
    private OrgUserConfigService orgUserConfigService;
    @Value("${localOrgNodeDisplay}")
    private boolean localOrgNodeDisplay;

    @Override
    public void add(AddVmNodeRequest request) throws MyHttpException {
        log.info("add vm node({})", request.toString());
        addNodeVerify(request);
        if (!localOrgNodeDisplay && StringUtils.isBlank(request.getParentId())) {
            request.setPermissionSwitch(1);
        }
        VmNodeData vmNodeData = new VmNodeData();
        ReflectUtil.copyPropertiesSimple(request, vmNodeData);
        BaseState baseState = null;
        try {
            baseState = vmNodeDataService.add(vmNodeData);
        } catch (Exception e) {
            throw new MyHttpException(409, e.getMessage());
        }
        if (baseState.getCode() == 1) {
            log.info("node add success");
            //节点添加成功后
            vmNodeTokenManagerService.del(vmNodeData.getId());
            nodeManageService.addVmNode(vmNodeData.getId());

        } else {
            log.info("node add fail,error:{}", baseState.getMsg());
            throw new MyHttpException(409, baseState.getMsg());
        }
    }

    public void addNodeVerify(AddVmNodeRequest request) throws MyHttpException {
        //如果配置了分职，则无法添加虚拟节点
        if (orgUserConfigService.hasOrgUser()) {
            throw new MyHttpException(409, GwErrorCode.CANT_CONFIGURED_VM_NODE_ORG.toString());
        }
        String authorization = AuthorizationUtil.getAuthorization(request.getUsername(), request.getPassword(), false);
        if ((StringUtils.isEmpty(request.getParentId()) && vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().isNull(VmNodeData::getParentId).or().eq(VmNodeData::getParentId,"")))
        ||(!StringUtils.isEmpty(request.getParentId()) && !vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().isNull(VmNodeData::getParentId).or().eq(VmNodeData::getParentId,"")))) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NODE_EXISTS.toString());
        }

        if (vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getName, request.getName()))) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NAME_EXISTS.toString());
        }
        if (vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getOrgId, request.getOrgId()))) {
            throw new MyHttpException(409, GwErrorCode.ORGID_EXISTS.toString());
        }
        if (vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getUsername, request.getUsername()))) {
            throw new MyHttpException(409, GwErrorCode.USERNAME_EXISTS.toString());
        }
        if (StringUtils.isEmpty(request.getAreaCode()) || vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getAreaCode, request.getAreaCode()))) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_AREA_CODE_EXISTS.toString());
        }
//        检测地区编码是否与组织树有重复
        GwNode gwNode = nodeManageService.getOrganizationNode();
        checkAreaCodeAndName(gwNode, request.getAreaCode(), request.getName(), null);

        try {
            GetTokenResponse response = smcLoginService.getTokens(authorization);
            if (response == null || response.getUuid() == null) {
                throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_IP_ERROR.toString());
            }
        } catch (MyHttpException e) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_ACCOUNT_PASSWORD_ERROR.toString());
        }


    }

    private void checkAreaCodeAndName(GwNode gwNode, String areaCode, String name, String id) throws MyHttpException {
        if (gwNode == null) {
            return;
        }
        boolean isSameNode = false;
        if(id == null || !gwNode.getId().equals(id)){
            isSameNode = true;
        }
        if(areaCode.equals(gwNode.getAreaCode()) && isSameNode){
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_AREA_CODE_EXISTS.toString());
        }
        if(gwNode.getName().equals(name) && isSameNode){
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NAME_EXISTS.toString());
        }

        List<GwNode> child = gwNode.getChild();
        if(child == null || child.isEmpty()){
            return;
        }
        for(GwNode childNode : child){
            checkAreaCodeAndName(childNode, areaCode, name, id);
        }
    }

    @Override
    public void update(UpdateVmNodeRequest request) throws MyHttpException {
        log.info("update vm node({})", request.toString());
        VmNodeData old = vmNodeDataService.getOneById(request.getId());
        if (StringUtils.isEmpty(request.getPassword()) || "******".equals(request.getPassword())) {
            request.setPassword(old.decryptPassword());
        }
        updateNodeVerify(request);
        if (!localOrgNodeDisplay && StringUtils.isBlank(old.getParentId())) {
            request.setPermissionSwitch(1);
        }
        old.setUsername(request.getUsername());
        old.setPassword(request.getPassword());
        old.setName(request.getName());
        old.setAreaCode(request.getAreaCode());
        old.setOrgId(request.getOrgId());
        old.setPermissionSwitch(request.getPermissionSwitch());
        BaseState baseState = vmNodeDataService.update(old);
        if (baseState.getCode() == 1) {
            vmNodeTokenManagerService.del(old.getId());
            nodeManageService.addVmNode(old.getId());
            log.info("vm node update success");
        } else {
            log.info("vm node update fail,error{}", baseState.getMsg());
            throw new MyHttpException(409, baseState.getMsg());
        }
    }




    public void updateNodeVerify(UpdateVmNodeRequest request) throws MyHttpException {
        VmNodeData old = vmNodeDataService.getOneById(request.getId());
        if (StringUtils.isEmpty(request.getPassword())) {
            request.setPassword(old.decryptPassword());
        }
        String authorization = AuthorizationUtil.getAuthorization(request.getUsername(), request.getPassword(), false);

        if (!request.getName().equals(old.getName()) && vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getName, request.getName()))) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NAME_EXISTS.toString());
        }
        if (!request.getAreaCode().equals(old.getAreaCode()) && vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getAreaCode, request.getAreaCode()))) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_AREA_CODE_EXISTS.toString());
        }
        if (vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getOrgId, request.getOrgId()).ne(VmNodeData::getId, request.getId()))) {
            throw new MyHttpException(409, GwErrorCode.ORGID_EXISTS.toString());
        }
        if (vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getUsername, request.getUsername()).ne(VmNodeData::getId, request.getId()))) {
            throw new MyHttpException(409, GwErrorCode.USERNAME_EXISTS.toString());
        }
        if(!request.getAreaCode().equals(old.getAreaCode())){
            GwNode gwNode = nodeManageService.getOrganizationNode();
            checkAreaCodeAndName(gwNode, request.getAreaCode(), request.getName(), request.getId());
        }

        try {
            smcLoginService.getTokens(authorization);
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
        if (id == null || vmNodeDataService.any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getParentId, id))) {
            throw new MyHttpException(409, GwErrorCode.NODE_CONFIG_NOT_EXISTS.toString());
        }
        VmNodeData old = new VmNodeData();
        BeanUtils.copyProperties(vmNodeDataService.getOneById(id), old);
        log.info("del node(id:{} name:{},code:{})", old.getId(), old.getName(), old.getAreaCode());
        nodeManageService.del(old.getId(), old.getName(), NodeType.LOW);
        vmNodeTokenManagerService.del(old.getId());
        BaseState baseState = vmNodeDataService.del(id);
        if (baseState.getCode() == 1) {
            nodeManageService.autoPush();
            log.info("del node success");
        } else {
            log.info("del update fail,error{}", baseState.getMsg());
            throw new MyHttpException(409, baseState.getMsg());
        }
    }

    @Override
    public VmNodeData getOneById(String id) {
        return vmNodeDataService.getOneById(id);
    }

    @Override
    public VmDetailsResponse list() {
        List<VmNodeData> list = vmNodeDataService.getListByLambda(new LambdaQueryWrapper<VmNodeData>().orderByAsc(VmNodeData::getName));
        if(list.isEmpty()){
            return null;
        }
        VmNodeData rootNode = null;
        for(VmNodeData vmNodeData: list){
            if(StringUtils.isNotEmpty(vmNodeData.getParentId())){
                continue;
            }
            rootNode = vmNodeData;
            break;
        }
        if(rootNode == null){
            log.info("rootNode is null:{}", list);
            return null;
        }

        VmDetailsResponse vmDetailsResponse = new VmDetailsResponse();
        BeanUtils.copyProperties(rootNode, vmDetailsResponse);
        VmDetailsResponse vmNodeTree = buildVmNodeDataTree(list, vmDetailsResponse);
        return vmNodeTree;
    }

    private VmDetailsResponse buildVmNodeDataTree(List<VmNodeData> list, VmDetailsResponse vmDetailsResponse){
        if(list == null || list.isEmpty()){
            return vmDetailsResponse;
        }
        Iterator<VmNodeData> it = list.iterator();
        while (it.hasNext()){
            VmNodeData vmNodeData = it.next();
            if(vmNodeData.getParentId() == null || !vmNodeData.getParentId().equals(vmDetailsResponse.getId())){
                continue;
            }
            VmDetailsResponse vmNode = new VmDetailsResponse();
            BeanUtils.copyProperties(vmNodeData, vmNode);
            vmDetailsResponse.getChild().add(vmNode);
            it.remove();
        }
        if(vmDetailsResponse.getChild().size()>0){
            for(VmDetailsResponse childVmNode: vmDetailsResponse.getChild()){
                buildVmNodeDataTree(list, childVmNode);
            }
        }
        return vmDetailsResponse;
    }
}

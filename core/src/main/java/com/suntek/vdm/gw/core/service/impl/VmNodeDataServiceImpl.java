package com.suntek.vdm.gw.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.core.mapper.VmNodeDataMapper;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.pojo.TableDate;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.VmNodeDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@CacheConfig(cacheNames = {"cas"}, cacheManager = "cacheManager")
@Slf4j
public class VmNodeDataServiceImpl extends ServiceFactoryImpl<VmNodeDataMapper, VmNodeData> implements VmNodeDataService {
    @Autowired
    private LocalTokenManageService localTokenManageService;

    @Override
    public TableDate getPage(Map<String, Object> query, Integer current, Integer size, String order, String orderType) {
        return null;
    }

    @Override
    @CacheEvict(allEntries = true)
    public BaseState add(VmNodeData vmNodeData) {
        if (any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getName, vmNodeData.getName()))) {
            return fail(GwErrorCode.NODE_CONFIG_NAME_EXISTS.toString());
        }
        if (any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getAreaCode, vmNodeData.getAreaCode()))) {
            return fail(GwErrorCode.NODE_CONFIG_AREA_CODE_EXISTS.toString());
        }
        try {
            vmNodeData.encryptPassword();
            vmNodeData.setSecurityVersion(CoreConfig.SECURITY_VERSION);//设置安全版本 为以后做兼容
            vmNodeData.setEnType(CoreConfig.EN_TYPE);
        } catch (Exception e) {
            log.error("Encrypted user data error:{}", e);
            return fail(GwErrorCode.UNKNOWN_ERROR.toString());
        }
        Date date = new Date();
        vmNodeData.setCreateTime(date);
        vmNodeData.setUpdateTime(date);
        return state(save(vmNodeData));
    }


    @Override
    @CacheEvict(allEntries = true)
    public BaseState update(VmNodeData info) {
        VmNodeData old = getById(info.getId());
        if (!info.getName().equals(old.getName()) && any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getName, info.getName()))) {
            return fail(GwErrorCode.NODE_CONFIG_NAME_EXISTS.toString());
        }
        if (!info.getAreaCode().equals(old.getAreaCode()) && any(new LambdaQueryWrapper<VmNodeData>().eq(VmNodeData::getAreaCode, info.getAreaCode()))) {
            return fail(GwErrorCode.NODE_CONFIG_AREA_CODE_EXISTS.toString());
        }
        old.setName(info.getName());
        old.setAreaCode(info.getAreaCode());
        old.setUsername(info.getUsername());
        info.encryptPassword();//加密密码
        old.setPassword(info.getPassword());
        old.setUpdateTime(new Date());
        old.setPermissionSwitch(info.getPermissionSwitch());
        old.setOrgId(info.getOrgId());
        return state(updateById(old));
    }

    @Override
    public VmNodeData getOneByToken(String token){
        LocalToken localToken = localTokenManageService.get(token);
        String orgId = localToken == null ? null : localToken.getOrgId();
        return getOneByOrgId(orgId);
    }

    @Override
    public boolean hasVmNode() {
        return count(new LambdaQueryWrapper<>()) > 0;
    }
    @Override
    public VmNodeData getOneByOrgId(String orgId){
        if (orgId == null) {
            return null;
        }
//        List<T> list = getListByLambda(new LambdaQueryWrapper<T>().eq(VmNodeData::getOrgId, orgId));
        QueryWrapper<VmNodeData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("org_id", orgId);
        List<VmNodeData> list = super.list(queryWrapper);
        return list == null || list.size() < 1 ? null : list.get(0);
    }

    @Override
    public VmNodeData getRootVmNode(){
        List<VmNodeData> list = getAll().stream().filter(item -> item.getParentId() == null || "".equals(item.getParentId())).collect(Collectors.toList());
        return list.size() < 1 ? null : list.get(0);
    }
}

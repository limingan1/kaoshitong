package com.suntek.vdm.gw.core.service.orgUser;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.core.entity.OrgUserData;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.core.mapper.OrgUserDataMapper;
import com.suntek.vdm.gw.core.mapper.VmNodeDataMapper;
import com.suntek.vdm.gw.core.pojo.TableDate;
import com.suntek.vdm.gw.core.service.VmNodeDataService;
import com.suntek.vdm.gw.core.service.impl.ServiceFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

@Service
@CacheConfig(cacheNames = {"cas"}, cacheManager = "cacheManager")
@Slf4j
public class OrgUserDataServiceImpl extends ServiceFactoryImpl<OrgUserDataMapper, OrgUserData> implements OrgUserDataService {

    @Override
    public TableDate getPage(Map<String, Object> query, Integer current, Integer size, String order, String orderType) {
        return null;
    }

    @Override
    public BaseState add(OrgUserData info) {
        if (any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getName, info.getName()).eq(OrgUserData::getNodeId, info.getNodeId()))) {
            return fail(GwErrorCode.NAME_EXISTS.toString());
        }
        if(any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getOrgId, info.getOrgId()).eq(OrgUserData::getNodeId, info.getNodeId()))){
            return fail(GwErrorCode.ORGID_EXISTS.toString());
        }
        if(any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getUsername, info.getUsername()).eq(OrgUserData::getNodeId, info.getNodeId()))){
            return fail(GwErrorCode.USERNAME_EXISTS.toString());
        }


        try {
            info.encryptPassword();
            info.setSecurityVersion(CoreConfig.SECURITY_VERSION);//设置安全版本 为以后做兼容
            info.setEnType(CoreConfig.EN_TYPE);
        } catch (Exception e) {
            log.error("Encrypted user data error:{}", e);
            return fail(GwErrorCode.UNKNOWN_ERROR.toString());
        }
        return state(save(info));
    }

    @Override
    public BaseState update(OrgUserData info) {
        OrgUserData old = getById(info.getId());
        if (any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getName, info.getName()).ne(OrgUserData::getId, info.getId()).eq(OrgUserData::getNodeId, info.getNodeId()))) {
            return fail(GwErrorCode.NAME_EXISTS.toString());
        }
        if(any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getOrgId, info.getOrgId()).ne(OrgUserData::getId, info.getId()).eq(OrgUserData::getNodeId, info.getNodeId()))){
            return fail(GwErrorCode.ORGID_EXISTS.toString());
        }
        if(any(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getUsername, info.getUsername()).ne(OrgUserData::getId, info.getId()).eq(OrgUserData::getNodeId, info.getNodeId()))){
            return fail(GwErrorCode.USERNAME_EXISTS.toString());
        }
        old.setName(info.getName());
        old.setUsername(info.getUsername());
        info.encryptPassword();//加密密码
        old.setPassword(info.getPassword());
        old.setOrgId(info.getOrgId());
        return state(updateById(old));
    }
}

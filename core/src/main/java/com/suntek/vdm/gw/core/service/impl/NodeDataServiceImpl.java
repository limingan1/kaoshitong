package com.suntek.vdm.gw.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.core.mapper.NodeDataMapper;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.pojo.TableDate;
import com.suntek.vdm.gw.core.service.NodeDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;


@Service
@CacheConfig(cacheNames = {"cas"}, cacheManager = "cacheManager")
@Slf4j
public class NodeDataServiceImpl extends ServiceFactoryImpl<NodeDataMapper, NodeData> implements NodeDataService {


    @Autowired
    private NodeDataMapper nodeDataMapper;

    @Override
    public TableDate getPage(Map<String, Object> query, Integer current, Integer size, String order, String orderType) {
        QueryWrapper<NodeData> queryWrapper = new QueryWrapper();
        orderInit(queryWrapper, order, orderType, NodeData.class);
        LambdaQueryWrapper<NodeData> lambdaQueryWrapper = queryWrapper.lambda();
        if (query.containsKey("name")) {
            lambdaQueryWrapper.like(NodeData::getName, query.get("name"));
        }
        if (query.containsKey("ip")) {
            lambdaQueryWrapper.like(NodeData::getIp, query.get("ip"));
        }
        if (query.containsKey("areaCode")) {
            lambdaQueryWrapper.like(NodeData::getAreaCode, query.get("areaCode"));
        }
        return pageService(lambdaQueryWrapper, current, size);
    }

    @Override
    @CacheEvict(allEntries = true)
    public BaseState add(NodeData info) {
        if (any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getName, info.getName()))) {
            return fail(GwErrorCode.NODE_CONFIG_NAME_EXISTS.toString());
        }
        if (any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getAreaCode, info.getAreaCode()))) {
            return fail(GwErrorCode.NODE_CONFIG_AREA_CODE_EXISTS.toString());
        }
        try {
            info.encryptPassword();
            info.setSecurityVersion(CoreConfig.SECURITY_VERSION);//设置安全版本 为以后做兼容
            info.setEnType(CoreConfig.EN_TYPE);
        } catch (Exception e) {
            log.error("Encrypted user data error:{}", e);
            return fail(GwErrorCode.UNKNOWN_ERROR.toString());
        }
        Date date = new Date();
        info.setCreateTime(date);
        info.setUpdateTime(date);
        return state(save(info));
    }

    /**
     * 必须使用解密的密码
     *
     * @param info
     * @return
     */
    @Override
    @CacheEvict(allEntries = true)
    public BaseState update(NodeData info) {
        NodeData old = getById(info.getId());
        if (!info.getName().equals(old.getName()) && any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getName, info.getName()))) {
            return fail(GwErrorCode.NODE_CONFIG_NAME_EXISTS.toString());
        }
        if (!info.getIp().equals(old.getIp()) && any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getIp, info.getIp()))) {
            return fail(GwErrorCode.NODE_CONFIG_IP_EXISTS.toString());
        }
        if (!info.getAreaCode().equals(old.getAreaCode()) && any(new LambdaQueryWrapper<NodeData>().eq(NodeData::getAreaCode, info.getAreaCode()))) {
            return fail(GwErrorCode.NODE_CONFIG_AREA_CODE_EXISTS.toString());
        }
        old.setName(info.getName());
        old.setAreaCode(info.getAreaCode());
        old.setIp(info.getIp());
        old.setUsername(info.getUsername());
        info.encryptPassword();//加密密码
        old.setPassword(info.getPassword());
        old.setUpdateTime(new Date());
        old.setPermissionSwitch(info.getPermissionSwitch());
        old.setVmrConfId(info.getVmrConfId());
        if (NodeBusinessType.isWelinkOrCloudLink(info.getBusinessType())) {
            old.setClientId(info.getClientId());
            old.setClientSecret(info.getClientSecret());
            old.setAddressBookUrl(info.getAddressBookUrl());
        }
        return state(updateById(old));
    }

    @Override
    @CacheEvict(allEntries = true)
    public BaseState del(String ids) {
        return state(super.removeByIds(idConvert(ids)));
    }

    @Override
    @Cacheable(key = "targetClass + methodName +#p0")
    public NodeData getLocal() {
        NodeData nodeData = getOne(new LambdaQueryWrapper<NodeData>().eq(NodeData::getType, NodeType.THIS.value()));
        return nodeData;
    }

    @Override
    @Cacheable(key = "targetClass + methodName +#p0")
    public NodeData getTop() {
        NodeData nodeData = getOne(new LambdaQueryWrapper<NodeData>().eq(NodeData::getType, NodeType.TOP.value()));
        return nodeData;
    }

    @Override
    @Cacheable(key = "targetClass + methodName +#p0")
    public List<NodeData> getLow() {
        List<NodeData> nodeData = getListByLambda(new LambdaQueryWrapper<NodeData>().eq(NodeData::getType, NodeType.LOW.value()));
        return nodeData;
    }

    @Override
    @Cacheable(key = "targetClass + methodName +#p0")
    public List<NodeData> getNotLocal() {
        List<NodeData> nodeData = getListByLambda(new LambdaQueryWrapper<NodeData>().ne(NodeData::getType, NodeType.THIS.value()));
        return nodeData;
    }

    @Override
    @Cacheable(key = "targetClass + methodName +#p0")
    public NodeData getOneByAreaCode(String areaCode) {
        LambdaQueryWrapper<NodeData> lambdaQueryWrapper = new LambdaQueryWrapper();
        lambdaQueryWrapper.eq(NodeData::getAreaCode, areaCode);
        return getOneByLambda(lambdaQueryWrapper);
    }


    @Override
    @Cacheable(key = "targetClass + methodName +#p0")
    public NodeData getOneByGwId(GwId gwId) {
        NodeData nodeData = getOneById(gwId.getNodeId());
        if (nodeData == null) {
            LambdaQueryWrapper<NodeData> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.or().eq(NodeData::getAreaCode, gwId.getAreaCode());
            nodeData = getOneByLambda(lambdaQueryWrapper);
        }
        return nodeData;
    }


    @Override
    @CacheEvict(allEntries = true)
    public void updatePrimaryKey(String newId, String oldId) {
        NodeData nodeData = getOneById(oldId);
        UpdateWrapper<NodeData> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", oldId).set("id", newId);
        nodeData.setUpdateTime(new Date());
        nodeDataMapper.update(nodeData, updateWrapper);
    }

    @Override
    public boolean isSuperTop() {
        if (getTop() == null) {
            return true;
        } else {
            return false;
        }
    }
}



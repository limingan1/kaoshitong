package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.VmNodeDataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class BaseServiceImpl {
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private VmNodeDataService vmNodeDataService;

    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private NodeManageService nodeManageService;

    /**
     * 判断请求是否属于本级
     *
     * @return
     */
    public boolean requestFormLocal(String nodeId) {
        if (StringUtils.isNotBlank(nodeId)) {
            NodeData nodeData = nodeDataService.getLocal();
            if (nodeData.getId().equals(nodeId)) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    /**
     * 替换本地Token为SmcToken
     *
     * @param token
     * @return
     */
    public String getSmcToken(String token) {
        return localTokenManageService.getSmcToken(token);
    }


    /**
     * 获取节点会议实现类型
     *
     * @param gwId
     * @return
     */
    public NodeBusinessType getNodeBusinessType(GwId gwId) {
        if (gwId == null) {
            return NodeBusinessType.SMC;
        }
        NodeData nodeData = nodeDataService.getOneByGwId(gwId);
        if (nodeData == null) {
            VmNodeData vmNode = vmNodeDataService.getOneById(gwId.getNodeId());
            log.error("get vmNode business type:{}", vmNode);
            if (vmNode != null || nodeManageService.getByNodeId(gwId.getNodeId()) != null) {
                return NodeBusinessType.SMC;
            }
            log.error("get node business type error:{}", gwId.toString());
        }
        return NodeBusinessType.valueOf(nodeData.getBusinessType());
    }
}

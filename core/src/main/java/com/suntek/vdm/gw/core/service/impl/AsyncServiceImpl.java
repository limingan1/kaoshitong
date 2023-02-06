package com.suntek.vdm.gw.core.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suntek.vdm.gw.core.api.request.node.GetNodeTokenRequest;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.enums.NodeStatusType;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.core.service.AsyncService;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.NodePushService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;


@Service
public class AsyncServiceImpl implements AsyncService {

    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private NodePushService nodePushService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private RemoteTokenManageService remoteTokenManageService;

    @Override
    @Async("taskExecutor")
    public void getNodeTokensAfter(String ip, GetNodeTokenRequest request) {
        NodeData nodeData = nodeDataService.getOneById(request.getId());
        if (nodeData == null) {
            //再次匹配
            nodeData = nodeDataService.getOneByLambda(new LambdaQueryWrapper<NodeData>().eq(NodeData::getIp, ip).eq(NodeData::getAreaCode, request.getAreaCode()));
        }
        if (nodeData != null) {
            //修改数据库主键
            String oldId = nodeData.getId();
            if (!oldId.equals(request.getId())) {
                nodeDataService.updatePrimaryKey(request.getId(), oldId);
                nodeData = nodeDataService.getOneById(request.getId());
                routManageService.cleanRoute(oldId);
            }
            //检测远端信息是否变化
            nodeManageService.checkNodeInfoChange(request.getName(), request.getAreaCode(), request.getSmcVersion(), nodeData);
            nodeManageService.setNodeInStatus(request.getId(), NodeStatusType.ONLINE);
            //如果登录的是上级成功  发送本地树木
            if (nodeData.getType().equals(NodeType.TOP.value())) {
                nodePushService.sendTopTree(nodeManageService.getFullLocalGwNode());
            }
            if (!oldId.equals(request.getId())) {
                remoteTokenManageService.removeNodeKeepAliveInfoById(oldId);
            }
            remoteTokenManageService.getNodeKeepAliveInfoById(request.getId()).success();
        }
    }
}

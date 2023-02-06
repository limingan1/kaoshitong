package com.suntek.vdm.gw.core.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.core.api.request.node.RemoteNodeUpdateRequest;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.enums.CoreApiUrl;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.NodePushService;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class NodePushServiceImpl implements NodePushService {

    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    @Lazy
    private NodeManageService nodeManageService;

    @Override
    @Async("taskExecutor")
    public void sendTopTree(GwNode tree) {
        log.info("Report local tree to top");
        try {
            remoteGwService.toTop().request(CoreApiUrl.SEND_TOP_TREE.value(), tree, HttpMethod.POST);
        } catch (MyHttpException e) {
            nodeManageService.setOrganizationNode(tree);
            nodeManageService.sendLowTreeHandler(tree);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendLowTree(GwNode organizationNode) {
        for (GwNode item : organizationNode.getChild()) {
            if(item.getIsVmNode() != null && item.getIsVmNode()){
                continue;
            }
            sendLowTree(new GwId(item.getId(), item.getAreaCode()),organizationNode);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendLowTree(GwId gwId, GwNode organizationNode) {
        log.info("Issued organizationTree  to low [id:{}]", gwId.toString());
        try {
            remoteGwService.toByGwId(gwId).request(CoreApiUrl.SEND_LOW_TREE.value(), organizationNode, HttpMethod.POST);
        } catch (MyHttpException exception) {
            log.error("error msg: {}", exception.getMessage());
            log.error("error stack: {}", exception.getStackTrace());
        }
    }


    /**
     * 通知远端本地节点更新
     */
    @Override
    @Async("taskExecutor")
    public void noticeRemoteLocalNodeUpdate() {
        log.info("Notice remote local node update");
        NodeData local = nodeDataService.getLocal();
        RemoteNodeUpdateRequest request = new RemoteNodeUpdateRequest();
        request.setAreaCode(local.getAreaCode());
        request.setName(local.getName());
        request.setId(local.getId());
        request.setSmcVersion(local.getSmcVersion());
        NodeData top = nodeDataService.getTop();
        if (top != null) {
            request.setFormType(NodeType.LOW.value());
            try {
                remoteGwService.toTop().request(CoreApiUrl.REMOTE_NODE_UPDATE.value(), request, HttpMethod.POST);
                log.info("Notice remote [code:{}] local node update success", top.getAreaCode());
            } catch (MyHttpException e) {
                log.info("Notice remote [code:{}] local node update fail,error:{}", top.getAreaCode(), e.toString());
                //TODO 失败的处理逻辑
            }
        }
        List<NodeData> lowS = nodeDataService.getLow();
        for (NodeData item : lowS) {
            request.setFormType(NodeType.TOP.value());
            try {
                remoteGwService.toByGwId(item.toGwId()).request(CoreApiUrl.REMOTE_NODE_UPDATE.value(), request, HttpMethod.POST);
                log.info("Notice remote [code:{}] local node update success", item.getAreaCode());
            } catch (MyHttpException e) {
                log.info("Notice remote [code:{}] local node update fail,error:{}", item.getAreaCode(), e.toString());
                //TODO 失败的处理逻辑
            }
        }
    }
}

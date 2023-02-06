package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.common.enums.NodeStatusType;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.common.pojo.node.NodeStatus;

public interface NodeManageService {

    void setOrganizationNode(GwNode organizationNode);

    GwNode getOrganizationNode();

    void add(String id);

    void update(String id);

    void del(String id, String name, NodeType type);

    GwNode getFullLocalGwNode();

    String loginNode(NodeData nodeData) throws MyHttpException;

    void sendTopTreeHandler(GwNode gwNode);

    void sendTopTreeVmHandler(GwNode gwNode);

    void sendLowTreeHandler(GwNode gwNode);

    void setNodeInStatus(String id, NodeStatusType statusType);

    NodeStatus getNodeStatus(String id);

    void checkNodeInfoChange(String name, String areaCode, String smcVersion, NodeData nodeData);

    public NodeBusinessType getNodeBusinessType(String id);

    GwNode getGwNodeById(String id);

    String getLoginMsg(String id);
    void setRemoteLoginMsg(String id, String msg);

    void addVmNode(String id);

    void loginVmNode(VmNodeData vmNodeData) throws MyHttpException;

    void autoPush();

    String getNodeNameByGwId(String id);

    GwNode getChildNodeByOrgId(String orgId);

    GwNode getByNodeId(String nodeId);

    GwNode getChildByNodeId(String nodeId);

    GwNode getNextUpperNode(String nodeId,boolean checkPermission);

    GwNode getCommonParentNode(String targetId, String sourceId);

    GwId findLocal(GwNode gwNode, String vmNodeId);
}

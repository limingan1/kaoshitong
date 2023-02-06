package com.suntek.vdm.gw.common.pojo.node;

import com.suntek.vdm.gw.common.pojo.GwId;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class GwNode {
    private String id;
    private String name;
    private String areaCode;
    private String ip;
    private String msg;
    private NodeStatus nodestatus;
    private String smcVersion;
    private int type;
    private Integer businessType;
    private Boolean isVmNode;
    private Integer permissionSwitch;
    private String parentId;
    private Boolean displayDirectlyUnder; //是否显示直属（只针对welink和华为云） true : 显示  false : 不显示
//    组织Id
    private String orgId;
    private List<GwNode> child;


    public List<GwNode> all() {
        List<GwNode> result = new ArrayList<>();
        result.add(this);
        if (child != null) {
            for (GwNode item : child) {
                result.addAll(item.all());
            }
        }
        return result;
    }

    public GwId toGwId(){
        return  new GwId(id,areaCode);
    }

    public GwNode getChildByOrgId(String orgId) {
        if (orgId == null || child == null) {
            return null;
        }
        for (GwNode gwNode : child) {
            if (orgId.equals(gwNode.getOrgId())) {
                return gwNode;
            }
            GwNode childByGwId = gwNode.getChildByOrgId(orgId);
            if (childByGwId != null) {
                return childByGwId;
            }
        }
        return null;
    }
    public GwNode getByNodeIdFromTree(String nodeId) {
        if (id.equals(nodeId)) {
            return this;
        }
        if (child == null || child.isEmpty()) {
            return null;
        }
        for (GwNode gwNode : child) {
            if (nodeId.equals(gwNode.getId())) {
                return gwNode;
            }
            GwNode child1 = gwNode.getByNodeIdFromTree(nodeId);
            if (child1 != null) {
                return child1;
            }
        }
        return null;
    }

    public GwNode getChildByNodeId(String nodeId) {
        if (nodeId == null || child == null || child.isEmpty()) {
            return null;
        }
        for (GwNode gwNode : child) {
            if (nodeId.equals(gwNode.getId())) {
                return gwNode;
            }
            GwNode childByGwId = gwNode.getChildByNodeId(nodeId);
            if (childByGwId != null) {
                return childByGwId;
            }
        }
        return null;
    }

    public GwNode getNextUpperNodeByNodeId(String childNodeId,boolean checkPermission){
        if (childNodeId == null || child == null || child.isEmpty()) {
            return null;
        }
        for (GwNode gwNode : child) {
            if (childNodeId.equals(gwNode.getId())) {
                if (checkPermission && new Integer(0).equals(gwNode.getPermissionSwitch())) {
                    System.out.println("gwNode = " + gwNode.getId() + "," + gwNode.getName() + "," + gwNode.getPermissionSwitch());
                    return null;
                }
                return this;
            }
            GwNode childByGwId = gwNode.getNextUpperNodeByNodeId(childNodeId,checkPermission);
            if (childByGwId != null) {
                return childByGwId;
            }
        }
        return null;
    }

    public boolean isVm() {
        return isVmNode != null && isVmNode;
    }
}

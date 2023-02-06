package com.suntek.vdm.gw.common.pojo.node;

import com.suntek.vdm.gw.common.enums.NodeStatusType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NodeStatus {
    public NodeStatusType in;
    public NodeStatusType out;

    public NodeStatus() {
        this.in = NodeStatusType.OFFLINE;
        this.out = NodeStatusType.OFFLINE;
    }
    public boolean  linked(){
        if (in.equals(NodeStatusType.ONLINE)&&out.equals(NodeStatusType.ONLINE)){
            return true;
        }else{
            return  false;
        }
    }
}

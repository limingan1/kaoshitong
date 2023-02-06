package com.suntek.vdm.gw.common.pojo;

import com.suntek.vdm.gw.common.pojo.node.NodeStatus;
import lombok.Data;

import java.util.List;

@Data
public class CascadeOrganization {
    private String casOrgId;
    private String casAreaCode;
    private String name;
    private Integer type;
    private boolean isLocal;
    private String msg;
    private NodeStatus nodestatus;
    private Integer businessType;
    private Boolean displayDirectlyUnder; //是否显示直属（只针对welink和华为云） true : 显示  false : 不显示
    private List<CascadeOrganization> child;
}

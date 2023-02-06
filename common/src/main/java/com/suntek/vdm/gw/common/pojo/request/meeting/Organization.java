package com.suntek.vdm.gw.common.pojo.request.meeting;

import lombok.Data;

@Data
public class Organization {
    /**
     * 组织ID(36字符)
     */
    private String id;

    private String name;

    private Double seqInParent;

    private String description;

    private Organization parent;
}
package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class CascadeTemplateBrief {
    /**
     * 会议Id
     */
    private String id;

    /**
     * 会议主题
     */
    private String subject;

    /**
     * 组织名称
     */
    private String organizationName;

    /**
     * 时长
     */
    private Integer duration;
}
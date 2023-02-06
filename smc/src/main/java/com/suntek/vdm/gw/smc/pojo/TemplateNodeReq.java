package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class TemplateNodeReq {
    /**
     * 本节点Id
     */
    private String templateId;

    /**
     * 父节点Id
     */
    private String parentTemplateId;

    /**
     * 会议类型
     */
    private String conferenceType;
}
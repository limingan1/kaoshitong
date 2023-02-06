package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class Node {
    /**
     * 本节点Id
     */
    private String templateId;

    /**
     * 会场主题
     */
    private String subject;

    /**
     * 父节点Id
     */
    private String parentTemplateId;

    /**
     * 会议类型
     */
    private String conferenceType;
}
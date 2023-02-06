package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class TemplateBriefInfo {
    /**
     * 模板ID
     */
    private String id;

    /**
     * 模板主题
     */
    private String subject;

    /**
     * 组织名称
     */
    private String organizationName;

    /**
     * 会议时长
     */
    private Integer duration;

    /**
     * 主服务区名称
     */
    private String mainServiceZoneName;

    /**
     * 会议类型
     */
    private String type;
}
package com.suntek.vdm.gw.common.pojo.request;

import lombok.Data;

@Data
public class ScheduleConfBrief {
    /**
     * 会议Id
     */
    private String id;

    /**
     * 会议Id(兼容旧版本)
     */
    private Integer legacyId;

    /**
     * 上级会议Id
     */
    private String parentId;

    /**
     * 会议主题
     */
    private String subject;

    /**
     * 创建者
     */
    private String username;

    /**
     * 创建者账户名
     */
    private String accountName;

    /**
     * 主席密码
     */
    private String chairmanPassword;

    /**
     * 来宾密码
     */
    private String guestPassword;

    /**
     * 会议开始时间
     */
    private String scheduleStartTime;

    /**
     * 会议时间类型
     */
    private String conferenceTimeType;

    /**
     * 时长
     */
    private Integer duration;

    /**
     * 会议类型
     */
    private String category;

    /**
     * 会议所处阶段
     */
    private String stage;

    /**
     * 组织名称
     */
    private String organizationName;

    /**
     * 会议是否激活
     */
    private Boolean active;

    /**
     * 会议接入号
     */
    private String accessCode;

    /**
     * 主服务区ID
     */
    private String mainServiceZoneId;

    /**
     * 主服务区名称
     */
    private String mainServiceZoneName;

    /**
     * 会议类型
     */
    private String type;
}
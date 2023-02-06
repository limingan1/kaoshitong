package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class ConferenceRsp {
    /**
     *会议Id
     */
    private String id;

    /**
     *会议Id(兼容旧版本)
     */
    private Integer legacyId;

    /**
     *上级会议Id
     */
    private String parentId;

    /**
     *会议主题
     */
    private String subject;

    /**
     *创建者
     */
    private String username;

    /**
     *创建者账户名
     */
    private String accountName;

    /**
     *主席密码
     */
    private String chairmanPassword;

    /**
     *来宾密码
     */
    private String guestPassword;

    /**
     *会议开始时间
     */
    private String scheduleStartTime;

    /**
     *时区Id
     */
    private String timeZoneId;

    /**
     *会议时间类型
     */
    private String conferenceTimeType;

    /**
     *周期会议时间参数
     */
    private PeriodConferenceTime periodConferenceTime;

    /**
     *时长
     */
    private Integer duration;

    /**
     *会议类型
     */
    private String category;

    /**
     *会议所处阶段
     */
    private String stage;

    /**
     *组织名称
     */
    private String organizationName;

    /**
     *会议是否激活
     */
    private Boolean active;

    /**
     *主会场
     */
    private String mainParticipantName;

}
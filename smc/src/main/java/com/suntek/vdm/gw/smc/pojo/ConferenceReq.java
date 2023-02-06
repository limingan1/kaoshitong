package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class ConferenceReq {
    /**
     * 会议主题(1~64字符)
     */
    private String subject;

    /**
     * 主席密码(长度为6位,0~9 之间的数字)(可选)
     */
    private String chairmanPassword;

    /**
     * 来宾密码(长度为6位,0~9 之间的数字)（可选）
     */
    private String guestPassword;

    /**
     * 会议开始时间(UTC时间,格式 为 yyyy-MM-dd HH:mm:ss z)（可选）
     */
    private String scheduleStartTime;

    /**
     * 时区Id（可选）
     */
    private String timeZoneId;

    /**
     * 会议时间类型
     */

    private String conferenceTimeType;

    /**
     * 周期会议时间参数（可选）
     */
    private PeriodConferenceTime periodConferenceTime;

    /**
     * 时长
     */
    private Integer duration;

    /**
     * vmr号码（可选）
     */
    private String vmrNumber;

    /**
     * 密级分类（1、公开；2、内部；3、秘密；4、机密）
     */
    private Integer securityLevel;

    /**
     * 是否显示密级
     */
    private Boolean showSecurityLevel;
}
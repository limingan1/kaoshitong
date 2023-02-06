package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class MultiConferenceServiceRsp {
    /**
     * 会议策略
     */
    private ConferencePolicyRsp conferencePolicySetting;

    /**
     * 会议能力
     */
    private ConferenceCapabilityRsp conferenceCapabilitySetting;

    /**
     * 会议接入号
     */
    private String accessCode;

    /**
     * 主MCU名称
     */
    private String mainMcuName;

    /**
     * 主服务区ID
     */
    private String mainServiceZoneId;

    /**
     * 主服务区名称
     */
    private String mainServiceZoneName;

    /**
     * 主席链接
     */
    private String chairmanLink;

    /**
     * 来宾链接
     */
    private String guestLink;
}
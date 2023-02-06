package com.suntek.vdm.gw.smc.response.meeting.templates;

import com.suntek.vdm.gw.smc.pojo.*;
import lombok.Data;

import java.util.List;

@Data
public class ConferencesToTemplateResponse {
    /**
     *会议模板与会人列表
     */
    private String id;
    /**
     *会议主题
     */
    private String subject;
    /**
     *主席密码
     */
    private String chairmanPassword;
    /**
     *来宾密码
     */
    private String guestPassword;
    /**
     *会议时长
     */
    private Integer duration;
    /**
     *组织ID
     */
    private String organizationId;
    /**
     *组织名称
     */
    private String organizationName;
    /**
     *主服务区ID
     */
    private String mainServiceZoneId;
    /**
     *主服务区名称
     */
    private String mainServiceZoneName;
    /**
     *主MCUID
     */
    private String mainMcuId;
    /**
     *主MCU名称
     */
    private String mainMcuName;

    /**
     *会议策略
     */
    private ConferencePolicyRsp conferencePolicySetting;

    /**
     *会议能力
     */
    private ConferenceCapabilityRsp conferenceCapabilitySetting;

    /**
     *会场列表
     */
    private List<TemplateParticipantRsp> templateParticipants;

    /**
     *与会人列表
     */
    private List<AttendeeRsp> templateAttendees;

    /**
     *流服务
     */
    private StreamServiceRsp streamService;

    /**
     *字幕服务
     */
    private SubtitleServiceRsp subtitleService;
}

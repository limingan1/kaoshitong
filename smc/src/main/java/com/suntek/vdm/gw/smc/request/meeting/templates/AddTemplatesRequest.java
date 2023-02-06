package com.suntek.vdm.gw.smc.request.meeting.templates;

import com.suntek.vdm.gw.smc.pojo.*;
import lombok.Data;

import java.util.List;

@Data
public class AddTemplatesRequest {
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
     * 会议时长(非永久会议15~1440分钟，永久会议2147483647分钟)
     */
    private Integer duration;

    /**
     * 组织Id(1~36字符)(可选)
     */
    private String organizationId;

    /**
     * 主服务区Id(1~36字符)（可选）
     */
    private String mainServiceZoneId;


    /**
     * 会议策略（可选）
     */
    private ConferencePolicyReq conferencePolicySetting;

    /**
     * 会议能力（可选）
     */
    private ConferenceCapabilityReq conferenceCapabilitySetting;

    /**
     * 会场列表（可选）
     */
    private List<TemplateParticipantReq> templateParticipants;

    /**
     * 与会人列表（可选）
     */
    private List<AttendeeReq> templateAttendees;

    /**
     * 录播推流参数（可选）
     */
    private StreamServiceReq streamService;

    /**
     * 字幕能力（可选）
     */
    private SubtitleServiceReq subtitleService;

//    /**
//     * 会议预置参数（可选）
//     */
//    private ConferencePresetReq confPresetParam;
    private String vmrNumber;
}

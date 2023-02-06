package com.suntek.vdm.gw.smc.response.meeting.templates;

import com.suntek.vdm.gw.smc.pojo.*;
import lombok.Data;

import java.util.List;

@Data
public class GetTemplatesResponse{
    /**
     * 会议模板Id
     */
    private String id;

    /**
     * 会议主题
     */
    private String subject;

    /**
     * 主席密码
     */
    private String chairmanPassword;

    /**
     * 来宾密码
     */
    private String guestPassword;

    /**
     * 会议时长
     */
    private Integer duration;

    /**
     * 组织Id
     */
    private String organizationId;

    /**
     * 组织名称
     */
    private String organizationName;

    /**
     * 主服务区Id
     */
    private String mainServiceZoneId;

    /**
     * 主服务区名称
     */
    private String mainServiceZoneName;

    /**
     * 会议策略
     */
    private ConferencePolicyRsp conferencePolicySetting;

    /**
     * 会议能力
     */
    private ConferenceCapabilityRsp conferenceCapabilitySetting;

    /**
     * 会场列表
     */
    private List<TemplateParticipantRsp> templateParticipants;

    /**
     * 与会人列表
     */
    private List<AttendeeRsp> templateAttendees;

    /**
     * 录播推流参数
     */
    private StreamServiceRsp streamService;

    private String vmrNumber;

}

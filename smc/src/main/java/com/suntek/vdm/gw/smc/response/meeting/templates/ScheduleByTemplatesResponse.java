package com.suntek.vdm.gw.smc.response.meeting.templates;

import com.suntek.vdm.gw.smc.pojo.*;
import lombok.Data;

import java.util.List;

@Data
public class ScheduleByTemplatesResponse {
    /**
     *会议响应基本信息
     */
    private ConferenceRsp conference;

    /**
     *多点会议响应
     */
    private MultiConferenceServiceRsp multiConferenceService;

    /**
     *流媒体会议响应
     */
    private StreamServiceRsp streamService;

    /**
     *签到服务响应
     */
    private CheckInServiceRsp checkInService;

    /**
     *与会者列表
     */
    private List<AttendeeRsp> attendees;

    /**
     *会场列表
     */
    private List<ParticipantRsp> participants;

    /**
     *字幕服务响应
     */
    private SubtitleServiceRsp subtitleService;

    /**
     *会议预置参数
     */
    private ConferencePresetRsp confPresetParam;
}

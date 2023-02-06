package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ScheduleConfRsp {
    /**
     * 会议基本信息
     */
    private ConferenceRsp conference;

    /**
     * 多点会议服务响应
     */
    private MultiConferenceServiceRsp multiConferenceService;

    /**
     * 流媒体服务响应
     */
    private StreamServiceRsp streamService;

    /**
     * 签到服务响应
     */
    private CheckInServiceRsp checkInService;

    /**
     * 会场列表
     */
    private List<ParticipantRsp> participants;

    /**
     * 与会人列表
     */
    private List<AttendeeRsp> attendees;

    /**
     * 字幕服务响应
     */
    private SubtitleServiceRsp subtitleService;
}
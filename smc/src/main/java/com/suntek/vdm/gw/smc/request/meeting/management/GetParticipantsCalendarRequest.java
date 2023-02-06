package com.suntek.vdm.gw.smc.request.meeting.management;

import lombok.Data;

import java.util.List;

@Data
public class GetParticipantsCalendarRequest {
    /**
     *会议室URI列表
     */
    private List<String> uriList;

    /**
     *会议开始时间(UTC时间, 格 式 为 yyyy-MM-dd HH:mm:ss z)
     */
    private String conferenceTime;

    /**
     *会议时长(大于0)
     */
    private Integer duration;

    /**
     *日历开始时间(UTC时间, 格 式 为 yyyy-MM-dd HH:mm:ss z)
     */
    private String startTime;

    /**
     *日历结束时间(UTC时间, 格 式 为 yyyy-MM-dd HH:mm:ss z)
     */
    private String endTime;
}

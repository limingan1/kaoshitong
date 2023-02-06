package com.suntek.vdm.gw.smc.response.meeting.management;

import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import lombok.Data;

import java.util.List;
@Data
public class GetParticipantsCalendarListResponse {
    /**
     * 标识
     */
    public String id;
    /**
     * 会议列表
     */
    public List<ScheduleConfBrief> conferenceList;
    /**
     * 闲置状态
     */
    public Boolean idleState;
}


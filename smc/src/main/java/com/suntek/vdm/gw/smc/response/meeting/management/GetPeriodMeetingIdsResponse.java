package com.suntek.vdm.gw.smc.response.meeting.management;


import lombok.Data;

import java.util.List;

@Data
public class GetPeriodMeetingIdsResponse  {
    /**
     * 子会议Id列表
     */
    private List<String> idLists;
    /**
     * 召开时间列表
     */
    private List<String> scheduleTimeLists;
}

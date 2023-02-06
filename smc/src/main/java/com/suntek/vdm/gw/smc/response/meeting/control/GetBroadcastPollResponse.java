package com.suntek.vdm.gw.smc.response.meeting.control;


import lombok.Data;

import java.util.List;

@Data
public class GetBroadcastPollResponse  {
    /**
     *间隔时间
     */
    private Integer interval;

    /**
     *轮询会场列表
     */
    private List<String> participantIds;

    /**
     *轮询操作
     */
    private String pollStatus;
}

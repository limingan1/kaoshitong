package com.suntek.vdm.gw.smc.request.meeting.control;

import lombok.Data;

import java.util.List;

@Data
public class SubscribeParticipantsStatusRequest {
    /**
     * 会场分页个数
     */
    private Integer pageSize;
    /**
     * 订阅会场列表
     */
   private List<String> participants;

}

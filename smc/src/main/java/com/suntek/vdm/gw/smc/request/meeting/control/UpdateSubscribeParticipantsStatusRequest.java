package com.suntek.vdm.gw.smc.request.meeting.control;

import lombok.Data;

import java.util.List;

@Data
public class UpdateSubscribeParticipantsStatusRequest {
    /**
     * 会场分页个数(不填）
     */
    private Integer pageSize;
    /**
     * 订阅会场Id列表
     */
    private List<String> participants;
}

package com.suntek.vdm.gw.smc.request.meeting.control;

import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import lombok.Data;

import java.util.List;

@Data
public class AddParticipantsRequest {
    /**
     * 会场请求参数列表(>=1长度)
     */
    private List<ParticipantReq> participants;
}

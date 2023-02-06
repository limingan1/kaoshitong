package com.suntek.vdm.gw.smc.request.meeting.control;

import lombok.Data;

import java.util.List;

@Data
public class ChatMicRequest {
    /**
     *  是否静音(静音：true/取消：false）
     */
    private Boolean set;
    /**
     * 被排除的会场ID列表
     */
    private List<String> excludeParticipants;
}

package com.suntek.vdm.gw.smc.request.meeting.management;

import lombok.Data;

@Data
public class SsoTicketAuthRequest {
    /**
     * * 鉴权TICKET凭据(64字符)
     */
    private String ticket;
    /**
     * 会议Id(36字符)
     */
    private String confId;
}

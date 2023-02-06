package com.suntek.vdm.gw.smc.request.meeting.management;

import lombok.Data;

@Data
public class GetSsoTokenTicketRequest {
    /**
     * 会议Id(36字符)
     */
   private String confId;
}

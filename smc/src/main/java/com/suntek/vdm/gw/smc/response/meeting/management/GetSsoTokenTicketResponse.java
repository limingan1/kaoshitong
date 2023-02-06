package com.suntek.vdm.gw.smc.response.meeting.management;


import lombok.Data;

@Data
public class GetSsoTokenTicketResponse  {
    /**
     * TICKET凭据
     */
    private String ticket;
}

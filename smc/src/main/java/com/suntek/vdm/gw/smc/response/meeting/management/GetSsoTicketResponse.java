package com.suntek.vdm.gw.smc.response.meeting.management;


import lombok.Data;

@Data
public class GetSsoTicketResponse  {
    /**
     * TICKET凭据
     */
    private String ticket;
}

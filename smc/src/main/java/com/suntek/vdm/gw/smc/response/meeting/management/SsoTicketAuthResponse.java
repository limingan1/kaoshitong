package com.suntek.vdm.gw.smc.response.meeting.management;


import lombok.Data;

@Data
public class SsoTicketAuthResponse  {
    /**
     * 操作描述
     */
    private String resultDesc;
    /**
     * 操作结果,非0 为失败
     */
    private Integer resultCode;

}

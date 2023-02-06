package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class CallInfoRsp {
    private ConferenceRsp conference;
    private String remoteUri;
}

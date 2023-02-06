package com.suntek.vdm.gw.common.pojo.request;

import lombok.Data;

@Data
public class GetChannelStatusReq {
    private String childCondId;
    private String confCasId;
}

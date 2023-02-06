package com.suntek.vdm.gw.common.pojo.request;

import lombok.Data;

@Data
public class DualHostDto {
    private String host;
    private Boolean isReady = false;
    private Boolean isGetKey;
    private String key;
}

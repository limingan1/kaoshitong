package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RestLoginResponse {
    /**
     * Token信息。
     */
    private TokenInfo data;

    /**
     * 地址本查询临时Token。
     */
    private String addressToken;

    /**
     * global外网IP
     */
    private String gloablPublicIP;
}
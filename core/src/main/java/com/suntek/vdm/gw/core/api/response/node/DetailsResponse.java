package com.suntek.vdm.gw.core.api.response.node;

import lombok.Data;

@Data
public class DetailsResponse {
    private String name;
    private String areaCode;
    private Integer type;
    private Integer businessType;
    private String ip;
    private int ssl;
    private String username;
    private String id;
    private Integer permissionSwitch;
    //welink节点使用
    private String vmrConfId;
    private String clientId;
    private String clientSecret;
    private String addressBookUrl;
    //welink节点使用 end...
}
package com.suntek.vdm.gw.welink.api.response;

import lombok.Data;

@Data
public class CorpBasicInfoDTO {
    private String id;
    private String name;
    private String address;
    private String adminName;
    private String account;
    private String phone;
    private String country;
    private String email;
    private Boolean enableSMS;
    private Boolean enableCloudDisk;
    private Boolean enablePstn;
    private Boolean autoUserCreate;
}

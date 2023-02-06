package com.suntek.vdm.gw.core.api.response.user;

import lombok.Data;

@Data
public class KeepALiveResponse {
    private  String uuid;
    private  String userType;
    private  String expire;
    private  String passwordExpireAfter;
}

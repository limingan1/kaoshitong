package com.suntek.vdm.gw.smc.response;


import lombok.Data;

@Data
public class GetTokenResponse  {
    private  String uuid;
    private  String userType;
    private  String expire;
    private  String passwordExpireAfter;
}

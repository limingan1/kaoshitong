package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WelinkAddressToken {
    private String accessToken; //access_token
    private Integer expireTime; //过期事件，单位：秒

    private String clientId;
    private String clientSecret;
    private String addressBookUrl;



    public WelinkAddressToken(String accessToken, Integer expireTime, String clientId, String clientSecret,String addressBookUrl) {
        this.accessToken = accessToken;
        this.expireTime = expireTime;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.addressBookUrl = addressBookUrl;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        if (accessToken != null) {
            sb.append("\"accessToken\":\"").append(accessToken).append("\",");
        }
        if (expireTime != null) {
            sb.append("\"expireTime\":").append(expireTime).append(",");
        }
        if (clientId != null) {
            sb.append("\"clientId\":\"").append(clientId).append("\",");
        }
        if (clientSecret != null) {
            sb.append("\"clientSecret\":\"").append(clientSecret).append("\",");
        }
        if (sb.lastIndexOf(",") != -1)
            sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append('}');
        return sb.toString();
    }
}

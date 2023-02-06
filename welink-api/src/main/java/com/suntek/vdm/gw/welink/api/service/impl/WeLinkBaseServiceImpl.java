package com.suntek.vdm.gw.welink.api.service.impl;

import com.suntek.vdm.gw.common.util.RandomId;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

public class WeLinkBaseServiceImpl {
    public MultiValueMap<String, String> tokenHandle(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.set("X-Auth-Token", token);
        String requestId = RandomId.getGUID();
        headers.set("RequestId",requestId);
        return headers;
    }
    public MultiValueMap<String, String> conferenceTokenHandle(String conferenceToken) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.set("X-Conference-Authorization", conferenceToken);
        String requestId = RandomId.getGUID();
        headers.set("RequestId",requestId);
        return headers;
    }
    public MultiValueMap<String, String> addressBookTokenHandle(String accessToken) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.set("Content-Type", "application/json;charset=UTF-8");
        headers.set("Accept-Charse", "UTF-8");
        headers.set("x-wlk-Authorization", accessToken);
        return headers;
    }
}

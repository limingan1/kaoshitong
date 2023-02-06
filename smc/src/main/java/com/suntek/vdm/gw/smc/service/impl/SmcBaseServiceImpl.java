package com.suntek.vdm.gw.smc.service.impl;


import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Slf4j
public class SmcBaseServiceImpl {
    public MultiValueMap<String, String> tokenHandle(String token) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.set("Token", token);
        return headers;
    }
}

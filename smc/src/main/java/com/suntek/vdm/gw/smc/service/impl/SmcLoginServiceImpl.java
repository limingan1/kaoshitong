package com.suntek.vdm.gw.smc.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.smc.adaptService.AdaptLoginService;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Slf4j
@Service
public class SmcLoginServiceImpl extends SmcBaseServiceImpl implements SmcLoginService {
    @Value("${useAdapt}")
    private Boolean useAdapt;

    @Autowired
    @Qualifier("smcHttpServiceImpl")
    private HttpService httpService;

    @Autowired
    AdaptLoginService adaptLoginService;

    @Override
    public GetTokenResponse getTokens(String authorization) throws MyHttpException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        if (authorization.contains("Basic")) {
            headers.set("Authorization", authorization);
        } else {
            headers.set("Authorization", "Basic " + authorization);
            authorization = "Basic " + authorization;
        }
        if(useAdapt){
            return adaptLoginService.getTokens(authorization);
        }else {
            String response = httpService.get("/tokens?clientType=portal", null, headers).getBody();
            return JSON.parseObject(response, GetTokenResponse.class);
        }
    }
    @Override
    public KeepALiveResponse keepAlive(String token) throws MyHttpException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.set("Token", token);
        String response = null;
        if(useAdapt){
            KeepALiveResponse keepALiveResponse = adaptLoginService.keepAlive(token);
            if(keepALiveResponse == null){
                throw new MyHttpException(401, "The requested session is empty.");
            }
            return keepALiveResponse;
        }else {
            response = httpService.put("/tokens?grantType=refresh", null, headers).getBody();
        }

        return JSON.parseObject(response, KeepALiveResponse.class);

    }

    @Override
    public boolean delTokens(String token) throws MyHttpException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.set("Token", token);
        if(useAdapt){
            return adaptLoginService.delTokens(token);
        }
        String response = httpService.delete("/tokens", null, headers).getBody();
        return "Logout success".equals(response);
    }


}

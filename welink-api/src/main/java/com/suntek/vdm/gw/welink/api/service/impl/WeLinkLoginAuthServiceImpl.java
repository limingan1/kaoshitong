package com.suntek.vdm.gw.welink.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.welink.api.service.WeLinkLoginAuthService;
import com.suntek.vdm.gw.welink.api.request.AuthRequest;
import com.suntek.vdm.gw.welink.api.response.AuthResponse;
import com.suntek.vdm.gw.welink.api.response.RefreshToKenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
@Slf4j
public class WeLinkLoginAuthServiceImpl extends WeLinkBaseServiceImpl implements WeLinkLoginAuthService {

    @Autowired
    @Qualifier("weLinkHttpServiceImpl")
    private HttpService httpService;

    /**
     * 执行鉴权
     * @param authRequest
     * @return
     * @throws MyHttpException
     */
    public AuthResponse auth(AuthRequest authRequest,String authorization) throws MyHttpException {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        if (authorization.contains("Basic")) {
            headers.set("Authorization", authorization);
        } else {
            headers.set("Authorization", "Basic " + authorization);
        }
        String response = httpService.post("/usg/acs/auth/account", authRequest, headers).getBody();
        return JSON.parseObject(response, AuthResponse.class);
    }

    public AuthResponse auth(AuthRequest authRequest) throws MyHttpException {
        String response = httpService.post("/usg/acs/auth/proxy", authRequest).getBody();
        return JSON.parseObject(response, AuthResponse.class);
    }


    /**
     * 刷新token
     * @param token
     * @return
     * @throws MyHttpException
     */
    public RefreshToKenResponse refreshToKen(String token) throws MyHttpException {
        String response = httpService.put("/usg/acs/token",null, tokenHandle(token)).getBody();
        return JSON.parseObject(response, RefreshToKenResponse.class);
    }

    /**
     * 注销登录
     * @param token
     * @throws MyHttpException
     */
    public void delToKen(String token) throws MyHttpException {
        String response = httpService.delete("/usg/acs/token",null, tokenHandle(token)).getBody();
    }
}

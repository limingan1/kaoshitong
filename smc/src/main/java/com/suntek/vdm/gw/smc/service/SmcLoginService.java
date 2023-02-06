package com.suntek.vdm.gw.smc.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;


public interface SmcLoginService {
    GetTokenResponse getTokens(String authorization) throws MyHttpException;
    KeepALiveResponse keepAlive(String token) throws MyHttpException;
    boolean delTokens(String token) throws MyHttpException;
}

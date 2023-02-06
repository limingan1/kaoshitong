package com.suntek.vdm.gw.smc.adaptService;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;

public interface AdaptLoginService {
    GetTokenResponse getTokens(String authorization) throws MyHttpException;
    KeepALiveResponse keepAlive(String token);
    boolean delTokens(String token);
}

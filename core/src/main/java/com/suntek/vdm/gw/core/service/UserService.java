package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.pojo.UserTickets;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;

import java.util.Map;

public interface UserService {
    GetTokenResponse getTokens(String authorization,boolean fromClientLogin, GwId realLocalGwId) throws BaseStateException;
    KeepALiveResponse keepAlive(String token) throws BaseStateException ;
    boolean delTokens(String token) throws BaseStateException ;
    UserTickets websocketPermissionCheck(Map<String, String[]> parameterMap);

}

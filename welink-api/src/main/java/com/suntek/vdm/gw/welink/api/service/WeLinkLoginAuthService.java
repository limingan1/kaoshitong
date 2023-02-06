package com.suntek.vdm.gw.welink.api.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.welink.api.request.AuthRequest;
import com.suntek.vdm.gw.welink.api.response.AuthResponse;
import com.suntek.vdm.gw.welink.api.response.RefreshToKenResponse;


public interface WeLinkLoginAuthService {
    /**
     * 执行鉴权
     * @param authRequest
     * @param authorization
     * @return
     * @throws MyHttpException
     */
    AuthResponse auth(AuthRequest authRequest,String authorization) throws MyHttpException;

    /**
     * 执行鉴权
     * @param authRequest
     * @return
     * @throws MyHttpException
     */
     AuthResponse auth(AuthRequest authRequest ) throws MyHttpException;

    /**
     * 刷新token
     * @param token
     * @return
     * @throws MyHttpException
     */
     RefreshToKenResponse refreshToKen(String token) throws MyHttpException;

    /**
     * 注销登录
     * @param token
     * @throws MyHttpException
     */
     void delToKen(String token) throws MyHttpException;

}

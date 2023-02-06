package com.suntek.vdm.gw.welink.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.welink.api.response.AuthResponse;

public interface WeLinkLinkService {
    AuthResponse login(String ip, String username, String password) throws MyHttpException;
    AuthResponse cloudLinkLogin(String ip, String username, String password) throws MyHttpException;
}
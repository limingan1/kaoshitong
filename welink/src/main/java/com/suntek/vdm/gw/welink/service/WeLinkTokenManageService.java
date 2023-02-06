package com.suntek.vdm.gw.welink.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.welink.api.response.AuthResponse;

public interface WeLinkTokenManageService {
    AuthResponse login(String ip, String username, String password) throws MyHttpException;
    AuthResponse cloudLinkLogin(String ip, String username, String password) throws MyHttpException;
    void restartKeepAlive();
    String updateToken();
    public String getToken();
}
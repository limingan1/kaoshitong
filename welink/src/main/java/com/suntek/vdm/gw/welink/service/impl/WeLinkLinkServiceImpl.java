package com.suntek.vdm.gw.welink.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.welink.api.response.AuthResponse;
import com.suntek.vdm.gw.welink.service.WeLinkTokenManageService;
import com.suntek.vdm.gw.welink.service.WeLinkLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WeLinkLinkServiceImpl implements WeLinkLinkService {
    @Autowired
    private WeLinkTokenManageService weLinkTokenManageService;

    public AuthResponse login(String ip, String username, String password) throws MyHttpException {
       return weLinkTokenManageService.login(ip, username, password);
    }

    public AuthResponse cloudLinkLogin(String ip, String username, String password) throws MyHttpException {
        return weLinkTokenManageService.cloudLinkLogin(ip, username, password);
    }

    public void restartKeepAlive(){
        weLinkTokenManageService.restartKeepAlive();
    }
}
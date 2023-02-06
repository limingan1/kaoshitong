package com.suntek.vdm.gw.welink.service.impl;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.util.AuthorizationUtil;
import com.suntek.vdm.gw.common.util.Encryption;
import com.suntek.vdm.gw.welink.api.request.AuthRequest;
import com.suntek.vdm.gw.welink.api.response.AuthResponse;
import com.suntek.vdm.gw.welink.api.response.RefreshToKenResponse;
import com.suntek.vdm.gw.welink.api.service.WeLinkLoginAuthService;
import com.suntek.vdm.gw.welink.api.service.impl.WeLinkHttpServiceImpl;
import com.suntek.vdm.gw.welink.pojo.WeLinkAccountInfo;
import com.suntek.vdm.gw.welink.pojo.WeLinkToken;
import com.suntek.vdm.gw.welink.service.WeLinkTokenManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WeLinkTokenManageServiceImpl implements WeLinkTokenManageService {


    private static WeLinkToken weLinkToken;
    /**
     * 默认保活时间300s
     */
    private static final long KEEP_ALIVE_INTERVAL = 300L;
    private ScheduledExecutorService scheduledExecutorService;

    @Autowired
    private WeLinkLoginAuthService weLinkLoginAuthService;

    public String getToken(){
        if(weLinkToken == null){
            return "";
        }
        return weLinkToken.getAccessToken();
    }

    public AuthResponse login(String ip, String username, String password) throws MyHttpException {
        //设置welink地址
        WeLinkHttpServiceImpl.setAddress(ip);
        AuthRequest authRequest = new AuthRequest();
        authRequest.setAccount(username);
        authRequest.setPwd(password);
        authRequest.setClientType(72);
        authRequest.setCreateTokenType(0);
        authRequest.setAuthServerType("workplace");
        authRequest.setAuthType("AccountAndPwd");
        AuthResponse response = weLinkLoginAuthService.auth(authRequest);
        weLinkToken = new WeLinkToken(response.getAccessToken(), response.getValidPeriod(), response.getExpireTime());
       return response;
    }

    public AuthResponse cloudLinkLogin(String ip, String username, String password) throws MyHttpException {
        //设置welink地址
        WeLinkHttpServiceImpl.setAddress(ip);
        AuthRequest authRequest = new AuthRequest();
        authRequest.setAccount(username);
        authRequest.setClientType(72);
        authRequest.setCreateTokenType(0);
        String authorization=Encryption.encryptBase64((username + ":" + password));
        authRequest.setAuthServerType("workplace");
        authRequest.setAuthType("AccountAndPwd");
        AuthResponse response = weLinkLoginAuthService.auth(authRequest, authorization);
        weLinkToken = new WeLinkToken(response.getAccessToken(), response.getValidPeriod(), response.getExpireTime());
        return response;
    }

    private ScheduledExecutorService create() {
        return new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("ka-welink-%d").build());
    }

    private void keepAlive() {
        log.info("welink token keepalive ing...");
        synchronized (this) {
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    if (weLinkToken == null) {
                        stopKeepAlive();
                        return;
                    }
                    updateToken();
                }
            }, KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL, TimeUnit.SECONDS);
        }
    }

    public String updateToken(){
        long validPeriod = weLinkToken.getValidPeriod();
        validPeriod = validPeriod - KEEP_ALIVE_INTERVAL;
        long currenTime = System.currentTimeMillis() / 1000;
        if (weLinkToken.getExpireTime() - currenTime > KEEP_ALIVE_INTERVAL * 2 && validPeriod > KEEP_ALIVE_INTERVAL * 2) {
            return weLinkToken.getAccessToken();
        }
        try {
            RefreshToKenResponse response = weLinkLoginAuthService.refreshToKen(weLinkToken.getAccessToken());
            weLinkToken = new WeLinkToken(response.getAccessToken(), response.getValidPeriod(), response.getExpireTime());
            return response.getAccessToken();
        } catch (MyHttpException e) {
            log.error("keepalive welink fail error:{}", e.getBody());
            weLinkToken = null;
        }
        return null;
    }

    public void startKeepAlive() {
        if (null == scheduledExecutorService || scheduledExecutorService.isShutdown()) {
            scheduledExecutorService = create();
            log.info("welink token keepalive start");
            keepAlive();
        }
    }

    public void stopKeepAlive() {
        if (scheduledExecutorService != null) {
            log.info("welink token keepalive stop");
            //立刻停止
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
    }

    public void restartKeepAlive() {
        stopKeepAlive();
        startKeepAlive();
    }
}
  
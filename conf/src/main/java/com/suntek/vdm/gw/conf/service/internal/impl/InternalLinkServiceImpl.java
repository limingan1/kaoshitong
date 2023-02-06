package com.suntek.vdm.gw.conf.service.internal.impl;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.util.AuthorizationUtil;
import com.suntek.vdm.gw.common.util.Encryption;
import com.suntek.vdm.gw.conf.service.SubscribeManageService;
import com.suntek.vdm.gw.conf.service.internal.InternalLinkService;
import com.suntek.vdm.gw.conf.service.internal.InternalSubscribeService;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.impl.LocalTokenManageServiceImpl;
import com.suntek.vdm.gw.smc.response.GetSystemTimeZoneResponse;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import com.suntek.vdm.gw.smc.service.SmcSystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class InternalLinkServiceImpl implements InternalLinkService {
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private SmcLoginService smcLoginService;
    @Autowired
    private SmcSystemConfigService smcSystemConfigService;
    @Autowired
    private InternalSubscribeService internalSubscribeService;
    @Autowired
    private SubscribeManageService subscribeManageService;


    private static Interner<String> pool = Interners.newWeakInterner();

    public void start() {
        synchronized (pool.intern(CoreConfig.INTERNAL_USER_TOKEN + "start")) {
            try {
                NodeData local = nodeDataService.getLocal();
                if (local == null) {
                    return;
                }
                String password = local.decryptPassword();
                String authorization = AuthorizationUtil.getAuthorization(local.getUsername(), password);
                GetTokenResponse response = smcLoginService.getTokens(authorization);
                localTokenManageService.add(CoreConfig.INTERNAL_USER_TOKEN, Encryption.decryptBase64(authorization.replace("Basic ", "")).split(":")[0], response.getUuid(), Long.valueOf(response.getExpire()), false, null);
                if (response == null) {
                    return;
                }
                try {
                    GetSystemTimeZoneResponse getSystemTimeZoneResponse = smcSystemConfigService.getSystemTimeZone(response.getUuid());
                    if(getSystemTimeZoneResponse != null){
                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                        Date date = simpleDateFormat.parse(getSystemTimeZoneResponse.getSystemTime());
                        long smcTime = date.getTime();
                        LocalTokenManageServiceImpl.timeDifference = System.currentTimeMillis() - smcTime;
                        log.info("smc and system time difference:{}ms", LocalTokenManageServiceImpl.timeDifference);
                    }
                } catch (MyHttpException | ParseException e) {

                }
                internalSubscribeService.init(local.getUsername());
            } catch (MyHttpException e) {
                subscribeManageService.disconnect(CoreConfig.INTERNAL_USER_TOKEN);
                localTokenManageService.del(CoreConfig.INTERNAL_USER_TOKEN);
                log.error("Internal login fail:{}", e.toString());
            }
        }
    }

    public void keepAlive() {
        synchronized (pool.intern(CoreConfig.INTERNAL_USER_TOKEN + "keepalive")) {
            LocalToken localToken = localTokenManageService.get(CoreConfig.INTERNAL_USER_TOKEN);
            //如果为空或者即将过期
            if (localToken == null || localToken.isExpire()) {
                start();
            } else {
                try {
                    log.info("send keepAlive.");
                    localTokenManageService.keepAlive(CoreConfig.INTERNAL_USER_TOKEN);
                    boolean openFlag = subscribeManageService.isOpen(CoreConfig.INTERNAL_USER_TOKEN);
                    if (!openFlag) {
                        new BaseStateException("Internal websocket check fail");
                    }
                } catch (BaseStateException e) {
                    log.error("Internal keepAlive fail: {}", e.getMessage());
                    subscribeManageService.disconnect(CoreConfig.INTERNAL_USER_TOKEN);
                    localTokenManageService.del(CoreConfig.INTERNAL_USER_TOKEN);
                    start();
                } catch (MyHttpException e) {
                    log.error("Internal keepAlive fail: {}", e.getMessage());
                    subscribeManageService.disconnect(CoreConfig.INTERNAL_USER_TOKEN);
                    localTokenManageService.del(CoreConfig.INTERNAL_USER_TOKEN);
                    start();
                }catch (Exception e){
                    log.error("Internal keepAlive fail: {}", e.getMessage());
                    subscribeManageService.disconnect(CoreConfig.INTERNAL_USER_TOKEN);
                    localTokenManageService.del(CoreConfig.INTERNAL_USER_TOKEN);
                    start();
                }
            }
        }
    }
}

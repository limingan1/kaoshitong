package com.suntek.vdm.gw.welink.service.impl;


import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.welink.api.pojo.WelinkAddressToken;
import com.suntek.vdm.gw.welink.api.response.UserDepartRes;
import com.suntek.vdm.gw.welink.api.service.WeLinkAddressBookService;
import com.suntek.vdm.gw.welink.service.WelinkAddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class WelinkAddressBookServiceImpl implements WelinkAddressBookService {

    @Autowired
    private WeLinkAddressBookService weLinkAddressBookService;

    private ScheduledExecutorService scheduledExecutorService;

    private WelinkAddressToken welinkAddressToken = null;
//    private static final String clientId1 = "20221012103250678266979";
//    private static final String clientSecret1 = "b8e23348-165d-4286-ba67-76b0b0df4d3a";

    /**
     * testFlag:是否是测试,如果是来自测试，则无需保活以及保存token
     * 获取access_token
     * @return access_token
     */
    @Override
    public String getTickets(String clientId,String clientSecret,String addressBookUrl,boolean testFlag) throws MyHttpException {
        if (notNull(clientId, clientSecret, addressBookUrl)) {
            WelinkAddressToken addressToken;
            try {
                addressToken = weLinkAddressBookService.getTickets(clientId, clientSecret, addressBookUrl);
            } catch (MyHttpException e) {
                switch (e.getCode()) {
                    case 500:
                    case 404:
                        throw new MyHttpException(409, GwErrorCode.PLEASE_CHECK_URL.toString());
                    default:
                        throw new MyHttpException(409, GwErrorCode.PLEASE_CHECK_CLIENT_ID.toString());
                }
            }
            String accessToken = addressToken.getAccessToken();
            if (testFlag) {
                return accessToken;
            }
            setWelinkAddressToken(addressToken);
            keepAlive();
            return accessToken;
        }else{
            if(testFlag){
                return null;
            }
            setWelinkAddressToken(null);
            stopKeepAlive();
        }
        return null;
    }

    private boolean notNull(String... arr) {
        for (String str : arr) {
            if (str == null || "".equals(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取用户id的所在部门编号
     */
    @Override
    public String getUserDepartCode(String userId, String accessToken,String addressBookUrl) throws MyHttpException {
        return weLinkAddressBookService.getUserDepartCode(userId, accessToken,addressBookUrl);
    }

    /**
     * 获取部门编号下所有人员详细信息
     * @param pageNo (默认为1)
     * @param pageSize (不能大于50)
     */
    @Override
    public UserDepartRes getUsersDepartAllInfo(String deptCode, String pageNo, String pageSize, String accessToken,String addressBookUrl) throws MyHttpException {
        return weLinkAddressBookService.getUsersDepartAllInfo(deptCode, pageNo, pageSize, accessToken,addressBookUrl);
    }

    @Override
    public WelinkAddressToken getWelinkAddressToken() {
        return welinkAddressToken;
    }

    public void setWelinkAddressToken(WelinkAddressToken welinkAddressToken) {
        this.welinkAddressToken = welinkAddressToken;
    }

    private void stopKeepAlive() {
        log.info("stop keep alive welink access token");
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
        }
    }
    private void start(){
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("welink-accessToken-%d").build());
        log.info("keepAlive welink access token... accessToken:{}", welinkAddressToken);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                getTickets(welinkAddressToken.getClientId(), welinkAddressToken.getClientSecret(), welinkAddressToken.getAddressBookUrl(),false);
            } catch (MyHttpException e) {
                e.printStackTrace();
            }
        }, welinkAddressToken.getExpireTime() - 400, 100L, TimeUnit.SECONDS);//指过期时间前400秒开始执行，每100秒保活一次，但是每次保活会终止线程池，然后重新保活

    }
    private void keepAlive() {
        stopKeepAlive();
        start();
    }

    @Override
    public void removeAccessToken() {
        setWelinkAddressToken(null);
        stopKeepAlive();
    }
}

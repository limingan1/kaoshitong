package com.suntek.vdm.gw.smc.adaptService.Impl;

import com.alibaba.fastjson.JSON;
import com.huawei.vdmserver.common.dto.AuthResponse;
import com.huawei.vdmserver.common.dto.KeepAliveResponse_3_0;
import com.huawei.vdmserver.common.dto.response.ErrorResponse;
import com.huawei.vdmserver.smc.core.service.AuthService;
import com.huawei.vdmserver.smc.core.service.SmcKeepAlive;
import com.huawei.vdmserver.smc.core.service.SmcLogOutService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.adaptService.AdaptLoginService;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.UnsupportedEncodingException;

@Service
@Slf4j
public class AdaptLoginServiceImpl implements AdaptLoginService {

    @Autowired
    @Qualifier("AuthService2.0")
    private AuthService authService;

    @Autowired
    @Qualifier("SmcKeepAlive2.0")
    SmcKeepAlive smcKeepAlive;

    @Autowired
    @Qualifier("SmcLogOutService2.0")
    SmcLogOutService smcLogOutService;

    @Override
    public GetTokenResponse getTokens(String authorization) throws MyHttpException {
        try {
            AuthResponse authResponse = authService.login(authorization, null);
            log.info("authResponse:{},{}", authResponse.getErrorCode(), authResponse.isIpLock());
            if (authResponse.getErrorCode() != null) {
                if (authResponse.getLockDuration() == null && !authResponse.isIpLock()) {
                    throw new MyHttpException(401, "{}");//提示账号密码错误
                }
                ErrorResponse errorResponse = new ErrorResponse();
                errorResponse.setIpLock(authResponse.isIpLock());
                if (authResponse.getLockDuration() == -1) {
                    errorResponse.setFailedNumber(0);
                }
                errorResponse.setBlockedStartTime(System.currentTimeMillis());
                errorResponse.setLockDuration(authResponse.getLockDuration());
                throw new MyHttpException(401, JSON.toJSONString(errorResponse));
            }
            GetTokenResponse getTokenResponse = new  GetTokenResponse();
            BeanUtils.copyProperties(authResponse, getTokenResponse);
            if(authResponse.getExpire()!=null){
                getTokenResponse.setExpire(authResponse.getExpire().toString());
            }
            return getTokenResponse;
        } catch (UnsupportedEncodingException | JAXBException  e) {
            log.error("login error: {}, {}", e.getMessage(),e.getStackTrace());
        }catch (MyHttpException e){
            throw e;
        }
        return null;
    }

    @Override
    public KeepALiveResponse keepAlive(String token) {
         KeepAliveResponse_3_0 keepAliveResponse_3_0 = smcKeepAlive.keepAlive(token, null);
         if(keepAliveResponse_3_0 == null){
             return null;
         }
        KeepALiveResponse keepALiveResponse = new  KeepALiveResponse();
        BeanUtils.copyProperties(keepAliveResponse_3_0, keepALiveResponse);
        if(keepAliveResponse_3_0.getExpire()!=null){
            keepALiveResponse.setExpire(keepAliveResponse_3_0.getExpire().toString());
        }
        return keepALiveResponse;
    }

    @Override
    public boolean delTokens(String token){
        String responseCode = smcLogOutService.logOut(token);
        if (responseCode.contains("success")) {
            return true;
        }
        return false;
    }
}

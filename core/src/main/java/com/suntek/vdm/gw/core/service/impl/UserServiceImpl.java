package com.suntek.vdm.gw.core.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.util.Encryption;
import com.suntek.vdm.gw.core.cache.CommonCache;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.pojo.UserTickets;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.UserService;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Autowired
    private SmcLoginService smcLoginService;
    @Autowired
    private LocalTokenManageService localTokenManageService;


    @Override
    public GetTokenResponse getTokens(String authorization, boolean fromClientLogin, GwId realLocalGwId) throws BaseStateException {
        try {
            if (authorization == null) {
                throw new BaseStateException("authorization is null");
            }
            GetTokenResponse response = smcLoginService.getTokens(authorization);
            String token = UUID.randomUUID().toString();
            localTokenManageService.add(token, Encryption.decryptBase64(authorization.replace("Basic ", "")).split(":")[0], response.getUuid(), Long.valueOf(response.getExpire()), fromClientLogin, realLocalGwId);
            response.setUuid(token);
            return response;
        } catch (MyHttpException e) {
            log.error("e: {}", e.getBody());
            String body = e.getBody();
            if (e.getBody().contains(" : ")) {
                body = e.getBody().split(" : ")[1];
                body = body.substring(1, body.length() - 1);
            }
            log.info("e.body:{}", body);
            throw new BaseStateException(body);
        }
    }


    @Override
    public KeepALiveResponse keepAlive(String token) throws BaseStateException {
        if (!localTokenManageService.contains(token)) {
            throw new BaseStateException("token not found");
        }
        try {
            KeepALiveResponse response = localTokenManageService.keepAlive(token);
            response.setUuid(token);
            return response;
        } catch (MyHttpException e) {
            throw new BaseStateException(e.getBody());
        }
    }

    @Override
    public boolean delTokens(String token) throws BaseStateException {
        try {
            boolean flag = smcLoginService.delTokens(localTokenManageService.getSmcToken(token));
            if (flag) {
                localTokenManageService.del(token);
            }
            return flag;
        } catch (MyHttpException e) {
            throw new BaseStateException(e.getBody());
        }
    }


    @Override
    public UserTickets websocketPermissionCheck(Map<String, String[]> parameterMap) {
        try {
            String timestamp = parameterMap.get("timestamp")[0];
            String signature = parameterMap.get("signature")[0];
            String username = parameterMap.get("username")[0];
            if (username == null || signature == null || timestamp == null) {
                return null;
            }
            Map<String, Map<String, String>> userTicketsMap = CommonCache.getUserTicketsMap();
            Map<String, String> userTicketsOneMap = userTicketsMap.get(username);
            if (userTicketsOneMap == null) {
                return null;
            }
            for (Map.Entry<String, String> entry : userTicketsOneMap.entrySet()) {
                String signatureSmc = Encryption.smcSignature(timestamp, username, entry.getValue(), entry.getKey());
                if (signature.equals(signatureSmc)) {
                    return new UserTickets(entry.getValue(), entry.getKey());
                }
            }
            return null;
        } catch (Exception e) {
            log.error("exception", e);
            return null;
        }
    }
}

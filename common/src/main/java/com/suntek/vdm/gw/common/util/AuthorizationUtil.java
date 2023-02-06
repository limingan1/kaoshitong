package com.suntek.vdm.gw.common.util;

import org.springframework.util.MultiValueMap;

public class AuthorizationUtil {

    public static String getAuthorization(String userName, String password) {
        return getAuthorization(userName, password, true);
    }

    public static String getAuthorization(String userName, String password, boolean hasBasic) {
        String result = Encryption.encryptBase64(userName + ":" + password);
        if (hasBasic) {
            result = "Basic " + result;
        }
        return result;
    }

    public static void setAuthorization(String userName, String password, MultiValueMap<String, String> headers) {
        setAuthorization(getAuthorization(userName, password),headers);
    }
    public static void setAuthorization(String authorization, MultiValueMap<String, String> headers) {
        headers.set("Authorization", authorization);
    }
}

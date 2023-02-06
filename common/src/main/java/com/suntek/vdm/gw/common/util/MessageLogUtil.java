package com.suntek.vdm.gw.common.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageLogUtil {
    public static void callback(String url, String body) {
        log.debug("Subscribe call back url:{}, body:{}", url, body);
    }
    public static void callback(String user,String subId, String url, String body) {
        log.debug("Subscribe call back user:{}, subId:{}, url:{}, body:{}", user,subId, url, body);
    }
    public static void callbackLocal(String user,String subId, String url, String body) {
        log.info("Subscribe call back local user:{}, subId:{}, url:{}, body:{}", user,subId, url, body);
    }
    public static void callbackNode(String json,String gwId) {
        log.debug("Push subscribe message gwId:{},message:{}", gwId.toString(), json);
    }
    public static void callbackRemote(String gwId, String body) {
        log.debug("Subscribe call back remote gwId:{}, body:{}", gwId, body);
    }
}

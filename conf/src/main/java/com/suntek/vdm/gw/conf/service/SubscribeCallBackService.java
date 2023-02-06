package com.suntek.vdm.gw.conf.service;

import java.util.Map;

public interface SubscribeCallBackService {
    void callback(String user, String subId, String url, String body, Map<String, String> headers);

    void callback(String url, String body, Map<String, String> headers);
}

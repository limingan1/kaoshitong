package com.suntek.vdm.gw.conf.service.internal;

public interface InternalCallBackService {
    void callBackController(String subId, String destination, String jsonData,String backDestination) ;
}
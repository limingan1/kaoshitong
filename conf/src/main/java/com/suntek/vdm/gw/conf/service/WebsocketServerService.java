package com.suntek.vdm.gw.conf.service;

import javax.websocket.Session;

public interface WebsocketServerService {
    void messageHandle(String message, Session session,String areaCode);
}

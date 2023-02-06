package com.suntek.vdm.gw.smc.ws.stomp.websocket;

public class SmcWebsocketClient {
    private static String token;

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        SmcWebsocketClient.token = token;
    }
}

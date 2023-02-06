package com.suntek.vdm.gw.conf.pojo;

import java.security.Principal;

public class  WebSocketUserAuthentication implements Principal {

    /**
     * 用户身份标识符
     */
    private String name;

    public WebSocketUserAuthentication(String name) {
        this.name = name;
    }

    /**
     * 获取用户登录令牌
     * @return
     */
    @Override
    public String getName() {
        return name;
    }
}

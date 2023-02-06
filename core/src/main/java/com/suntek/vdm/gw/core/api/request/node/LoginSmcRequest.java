package com.suntek.vdm.gw.core.api.request.node;

import lombok.Data;

@Data
public class LoginSmcRequest {
    private String username;
    private String password;
    private String ipAddress;
}

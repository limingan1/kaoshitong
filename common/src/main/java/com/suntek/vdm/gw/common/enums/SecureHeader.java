package com.suntek.vdm.gw.common.enums;

public enum SecureHeader {
    XContentTypeOptions("X-Content-Type-Options","nosniff"),
    XXSSProtection("X-XSS-Protection","1"),
    ContentSecurityPolicy("Content-Security-Policy","default-src 'self'; style-src 'self' 'unsafe-inline';script-src 'self' 'unsafe-eval' 'unsafe-inline';img-src  'self'  'unsafe-inline'  'unsafe-eval'  data:"),
    CacheControl("Cache-Control","no-store"),
    StrictTransportSecurity("Strict-Transport-Security","max-age=63072000; includeSubdomains; preload"),
//    XFrameOptions("X-Frame-Options","DENY"),
    XFrameOptionsSameOrigin("X-Frame-Options","SAMEORIGIN"),
    AccessControlAllowOrigin("Access-Control-Allow-Origin","DENY"),
    ;
    private String key;
    private String value;

    SecureHeader(String key,String value) {     //必须是private的，否则编译错误
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}

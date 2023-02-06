package com.suntek.vdm.gw.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RemoteToken {
    private String id;
    private String token;
    private String ip;
    private boolean ssl;
    private String areaCode;
    private long tokenUpdateTime;
    private long expire;


    public RemoteToken(String id, String token, String ip, boolean ssl, String areaCode, long expire) {
        this.id = id;
        this.token = token;
        this.ip = ip;
        this.ssl = ssl;
        this.areaCode = areaCode;
        this.tokenUpdateTime = System.currentTimeMillis();
        this.expire = expire;
    }

    public boolean isExpire() {
        //默认多判断3分钟
        return (tokenUpdateTime + (3 * 60 * 1000)) < System.currentTimeMillis();
    }
}

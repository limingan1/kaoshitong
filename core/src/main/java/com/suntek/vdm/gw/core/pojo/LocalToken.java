package com.suntek.vdm.gw.core.pojo;

import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.service.impl.LocalTokenManageServiceImpl;
import lombok.Data;

@Data
public class LocalToken {
    private String token;
    private String username;
    private String smcToken;
    private long expire;
    private String orgId;
    private GwId gwId;

    public LocalToken(String token, String username, String smcToken, long expire) {
        this.expire = expire;
        this.token = token;
        this.username = username;
        this.smcToken = smcToken;
     }

    /**
     * 是否过期  是 过期 否 过期
     * @return
     */
    public boolean isExpire() {
        long smcTime = System.currentTimeMillis() - LocalTokenManageServiceImpl.timeDifference;
        return this.expire < smcTime;
    }
}

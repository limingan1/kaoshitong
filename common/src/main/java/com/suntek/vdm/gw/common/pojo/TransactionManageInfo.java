package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class TransactionManageInfo {
    public long lastTime;
    public Boolean flag;

    public TransactionManageInfo() {
        lastTime = System.currentTimeMillis();
    }

    public boolean expired() {
        if ((lastTime + (1000 * 60 * 60)) < System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

}

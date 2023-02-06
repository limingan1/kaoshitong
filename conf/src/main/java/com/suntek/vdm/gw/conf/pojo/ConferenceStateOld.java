package com.suntek.vdm.gw.conf.pojo;

import lombok.Data;

@Data
public class ConferenceStateOld {
    private String pId;
    private long lastTime;


    public boolean expired() {
        return (lastTime + 1000) > System.currentTimeMillis();
    }
}

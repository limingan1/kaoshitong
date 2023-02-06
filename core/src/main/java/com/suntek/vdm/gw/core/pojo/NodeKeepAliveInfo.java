package com.suntek.vdm.gw.core.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class NodeKeepAliveInfo {
    private String id;
    private String name;
    private int failCount;
    private long nextTime;
    private long lastTime;


    public boolean need() {
        if (failCount == 0) {
            return true;
        }
        long timeDifference = nextTime- System.currentTimeMillis();
        if (timeDifference > 0) {
            log.info("keep-alive node（{} {}） after {} seconds", this.id, this.name, timeDifference / 1000);
            return false;
        } else {
            return true;
        }
    }

    public void fail() {
        this.failCount++;
        this.nextTime = System.currentTimeMillis() + getWaitTime();
        log.info("node keep alive fail id:{} {} fail count:{} next time:{}", this.id,this.name, this.failCount, this.nextTime);
    }

    public void success() {
        this.failCount = 0;
    }


    private long getWaitTime() {
        if (this.failCount < 3) {
            return 0;
        } else {
            return (long) Math.pow(2, this.failCount - 3) * 45 * 1000;
        }
    }
}

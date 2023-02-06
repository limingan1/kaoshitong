package com.suntek.vdm.gw.core.task;


import java.util.concurrent.ScheduledFuture;

public final class ScheduledTask {

    volatile ScheduledFuture<?> FUTURE;

    /**
     * 取消定时任务
     */
    public void cancel() {
        ScheduledFuture<?> future = this.FUTURE;
        if (future != null) {
            future.cancel(true);
        }
    }
}

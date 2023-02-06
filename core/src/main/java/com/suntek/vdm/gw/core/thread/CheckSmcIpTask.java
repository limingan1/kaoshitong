package com.suntek.vdm.gw.core.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledFuture;

@Service
@Slf4j
public class CheckSmcIpTask {
    @Qualifier("taskSchedulerCore")
    @Autowired
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    //检测SMCip地址否正常有效
    private ScheduledFuture<?> checkIpValidFuture;

    // 用来判断该线程是否停止
    private boolean isStop = false;

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }

    public void startCheckIpValidService(Runnable runnable){
        if (checkIpValidFuture != null){
            checkIpValidFuture.cancel(true);
            log.info("Stop checkIpValid service thread");
        }
        checkIpValidFuture = threadPoolTaskScheduler.schedule(runnable, new CronTrigger("0/30 * * * * *"));
        log.info("checkIpValid thread with start.");
    }

    public void stopCheckIpValidService(){
        if (checkIpValidFuture != null){
            checkIpValidFuture.cancel(true);
        }
        log.info("Stop checkIpValid thread ");
    }
}

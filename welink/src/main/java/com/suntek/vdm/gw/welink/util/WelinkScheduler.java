package com.suntek.vdm.gw.welink.util;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.suntek.vdm.gw.welink.websocket.WeLinkWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WelinkScheduler {
    /**
     * 默认保活时间
     */
    private static final long KEEP_ALIVE_INTERVAL = 20L;
    private static final int INIT_SIZE = 70;

    private ScheduledExecutorService scheduledExecutorService;

    private ScheduledExecutorService create() {
        return new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("ka-websocket-%d").build());
    }

    public void startKeepAlive(WeLinkWebSocketService weLinkWebSocketService) {

        synchronized (this) {
            //                    ergodicConference();
            log.info("[WelinkKeepWebSocketAliveService] startKeepAlive");
            scheduledExecutorService.scheduleAtFixedRate(weLinkWebSocketService::sendHeartbeat, 0, KEEP_ALIVE_INTERVAL, TimeUnit.SECONDS);
        }
    }


    /**
     * 取消任务
     */
    public void stop() {
        if (scheduledExecutorService != null) {
            log.info("[WelinkKeepWebSocketAliveService][KeepAliveService.startKeepAlive] stop...");
            //停止
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }

    public void start(WeLinkWebSocketService weLinkWebSocketService) {
        if (null == scheduledExecutorService || scheduledExecutorService.isShutdown()) {
            scheduledExecutorService = create();
            log.info("[WelinkKeepWebSocketAliveService][KeepAliveService.startKeepAlive] start...");
            startKeepAlive(weLinkWebSocketService);
        }
    }

    public void restart(WeLinkWebSocketService weLinkWebSocketService) {
        stop();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        start(weLinkWebSocketService);
    }
}

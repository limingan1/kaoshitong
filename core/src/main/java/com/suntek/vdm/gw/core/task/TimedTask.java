package com.suntek.vdm.gw.core.task;

import com.suntek.vdm.gw.common.util.TransactionManage;
import com.suntek.vdm.gw.core.cache.CommonCache;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.RemoteTokenManageService;
import com.suntek.vdm.gw.core.service.VmNodeTokenManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component("timedTask-core")
public class TimedTask {
    @Autowired
    private LocalTokenManageService localTokenManageService;


    /**
     * 本地token过期清理
     */
    @Scheduled(initialDelay = 10 * 1000, fixedRate = 120 * 1000)
    public void localTokenClean() {
        if (!CommonCache.LOAD_STATUS){
            return;
        }
        try {
            localTokenManageService.cleanExpired();
        } catch (Exception e) {
            log.error("[localTokenCheck] error:{}", e);
        }
    }

    /**
     * 清除本地事物过期的
     */
    @Scheduled(initialDelay = 10 * 1000, fixedRate = 60*60 * 1000)
    public void transactionManageInfoClean() {
        if (!CommonCache.LOAD_STATUS){
            return;
        }
        try {
            TransactionManage.clean();
            localTokenManageService.cleanExpired();
        } catch (Exception e) {
            log.error("[localTokenCheck] error:{}", e);
        }
    }
}

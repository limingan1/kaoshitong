package com.suntek.vdm.gw.conf.task;

import com.suntek.vdm.gw.conf.dual.TimerJobService;
import com.suntek.vdm.gw.conf.pojo.ReSubscribeInfo;
import com.suntek.vdm.gw.conf.service.SubscribeManageAsyncService;
import com.suntek.vdm.gw.conf.service.SubscribeManageService;
import com.suntek.vdm.gw.conf.service.impl.TempServiceImpl;
import com.suntek.vdm.gw.conf.service.internal.InternalLinkService;
import com.suntek.vdm.gw.core.cache.CommonCache;
import com.suntek.vdm.gw.core.service.RemoteTokenManageService;
import com.suntek.vdm.gw.core.service.VmNodeTokenManagerService;
import com.suntek.vdm.gw.core.service.orgUser.OrgUserTokenManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Queue;

@Slf4j
@Component("timedTask-conf")
public class TimedTask {
    @Autowired
    private SubscribeManageAsyncService subscribeManageAsyncService;
    @Autowired
    private SubscribeManageService subscribeManageService;
    @Autowired
    private VmNodeTokenManagerService vmNodeTokenManagerService;
    @Autowired
    private OrgUserTokenManagerService orgUserTokenManagerService;


    @Autowired
    private InternalLinkService internalLinkService;
    @Autowired
    private TempServiceImpl tempService;
    @Autowired
    private RemoteTokenManageService remoteTokenManageService;
    @Autowired
    private TimerJobService timerJobService;

    @Value("${cas.database.dual_computer_enable}")
    private Boolean dual_computer_enable;



    @Scheduled(initialDelay = 3 * 1000, fixedRate = 45 * 1000)
    public void internalKeepAlive() {
        if (!CommonCache.LOAD_STATUS || (dual_computer_enable != null && dual_computer_enable && !timerJobService.isMaster())){
            return;
        }
        try {
              internalLinkService.keepAlive();
        } catch (Exception e) {
            log.error("InternalKeepAlive error: {}", e);
        }
    }

    /**
     * 远端token保活
     */
    @Scheduled(initialDelay = 3 * 1000, fixedRate = 45 * 1000)
    public void remoteTokenKeepAlive() {
        if (!CommonCache.LOAD_STATUS || (dual_computer_enable != null && dual_computer_enable && !timerJobService.isMaster())){
            return;
        }
        try {
            remoteTokenManageService.keepAliveAll();
        } catch (Exception e) {
            log.error("remoteTokenKeepAlive error:{}", e);
        }
    }

    @Scheduled(initialDelay = 3 * 1000, fixedRate = 3 * 1000)
    public void   callCascadeChannel() {
        tempService.callCascadeChannel();
    }





    @Scheduled(initialDelay = 3 * 1000, fixedRate = 5)
    public void subscriptionRecover() {
        if (!CommonCache.LOAD_STATUS){
            return;
        }
        try {
            Queue<ReSubscribeInfo> reSubscribeInfoQueue = subscribeManageService.getReSubscribeInfoQueue();
            int size = reSubscribeInfoQueue.size();
            for (int i = 0; i < size; i++) {
                ReSubscribeInfo reSubscribeInfo = reSubscribeInfoQueue.poll();
                subscribeManageAsyncService.reconnect(reSubscribeInfo.getMyStompSession(), reSubscribeInfo.getStompSessionHandlerAdapter());
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            log.error("subscription recover error:{}", e.getMessage());
        }
    }


    /**
     * 虚拟节点token保活
     */
    @Scheduled(initialDelay = 3 * 1000, fixedRate = 45 * 1000)
    public void vmTokenKeepAlive() {
        if (!CommonCache.LOAD_STATUS){
            return;
        }
        vmNodeTokenManagerService.keepAliveAll();
    }

    /**
     * 分职账号token保活
     */
    @Scheduled(initialDelay = 3 * 1000, fixedRate = 45 * 1000)
    public void orgUserTokenKeepAlive() {
        if (!CommonCache.LOAD_STATUS){
            return;
        }
        orgUserTokenManagerService.keepAliveAll();
    }


}

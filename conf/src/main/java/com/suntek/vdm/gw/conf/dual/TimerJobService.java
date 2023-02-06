package com.suntek.vdm.gw.conf.dual;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.suntek.vdm.gw.common.util.dual.ExecUtil;
import com.suntek.vdm.gw.common.util.dual.Host;
import com.suntek.vdm.gw.common.util.dual.IpAddressUtils;
import com.suntek.vdm.gw.common.util.dual.TimeUtils;
import com.suntek.vdm.gw.core.task.Scheduler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TimerJobService {

    @Value("${cas.floating_ip}")
    private String floatingIp;
    @Value("${cas.security.file_path}")
    private String securityFilePath;

    @Autowired
    private Scheduler scheduler;
    @Autowired
    @Lazy
    private DualService dualService;
    @Autowired
    private ExecUtil execUtil;

    /**
     * 默认保活时间45s
     */
    private static final long KEEP_ALIVE_INTERVAL = 5L;

    /**
     * 20s后开始心跳，默认值
     */
    private static final long KEEP_ALIVE_DELAY = 5L;

    /**
     * 多少次websocket心跳发一次http心跳，防止http服务端的session超时
     */
    private static final int DEFAULT_TIME = 5;

    private int time = DEFAULT_TIME;

    private  int reTryTime = 0;

    public boolean isMaster() {
        return isMaster;
    }

    private boolean isMaster = true;

    private ScheduledExecutorService scheduledExecutorService;

    private ScheduledExecutorService create() {
        return new ScheduledThreadPoolExecutor(1, new ThreadFactoryBuilder().setNameFormat("ka-TimerJobService-%d").build());
    }


    private void startKeepAlive(long delay) {
        time = DEFAULT_TIME;
        if (delay < 0) {
            delay = KEEP_ALIVE_DELAY;
        }
        synchronized (this){
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run() {
                    try {
                        List<String> localIPList = IpAddressUtils.getLocalIPList();
                        boolean isHost = false;
                        for(String localIp : localIPList) {
                            if(localIp.equals(floatingIp)) {
                                isHost = true;
                                break;
                            }
                        }

                        if(isHost != isMaster) {
                            isMaster = isHost;
                            log.info( "The standby machine is switched to the host machine, and the cascade service initialization is performed.");
                            Host host = Host.getInstance();
                            //备机变主机
                            if(isHost){
                                // 关闭定时任务
//                                TimerJobService.getInstance().stopLater();
                                //等一段时间做发布
                                host.setMaster(isHost);
                                scheduler.scheduledThreadPoolLaterDo(0, TimeUnit.MINUTES, new Runnable() {
                                    @Override
                                    public void run() {
                                        String home = securityFilePath;
                                        String cmd = "cmd /c powershell "+home + "/dual/Scripts/Dual_Disable_Slave.ps1 " +
                                                host.getMasterHostName()+" "+host.getSlaveHostName()+" "+
                                                home+"/dual";
                                        try {
                                            log.info(cmd);
                                            String logString = execUtil.exeCommand(cmd);
                                            log.info(logString);
                                        }catch (Exception e){
                                            log.error("Exception{}",e.getMessage());
                                            log.error("Exception{}",e.getStackTrace());
                                        }
                                        Host host = Host.getInstance();
                                        dualService.excureCmd(true,host);
                                    }
                                });
                                try {
                                    dualService.start();
                                }catch (Exception e) {
                                    log.error("Exception{}",e.getMessage());
                                    log.error("Exception{}",e.getStackTrace());
                                }

                            }else{
                                //主机变备机
                                host.setMaster(isHost);
                                scheduler.scheduledThreadPoolLaterDo(0, TimeUnit.SECONDS, new Runnable() {
                                    @Override
                                    public void run() {
                                        String home = securityFilePath;
                                        String cmd = "cmd /c powershell "+home + "/dual/Scripts/Dual_Disable_Master.ps1 " +
                                                host.getMasterHostName()+" "+host.getSlaveHostName()+" "+
                                                home+"/dual";
                                        try {
                                            String logString = execUtil.exeCommand(cmd);
                                            log.info(logString);
                                        }catch (Exception e){
                                            log.error("Exception{}",e.getMessage());
                                            log.error("Exception{}",e.getStackTrace());
                                        }
                                        try {
                                            execUtil.openBat("\\tools\\restart.bat");
                                        } catch (Exception e) {
                                            System.out.println(e.getMessage());
                                            System.out.println(e);
                                        }
                                    }
                                });
                            }
                        }else{
                            if(reTryTime>=60){
                                reTryTime=0;
                                Host host = Host.getInstance();
                                dualService.excureCmd(isHost,host);
                            }else{
                                reTryTime++;
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }, delay, KEEP_ALIVE_INTERVAL, TimeUnit.SECONDS);
        }
    }

    public void stopLater() {
        if (scheduledExecutorService != null) {
            log.info("[TimerJobService.TimerJobService] stop...");
            //立刻停止
            scheduledExecutorService.shutdown();
            scheduledExecutorService = null;
        }
    }

    /**
     * 取消任务
     */
    public void stop() {
        if (scheduledExecutorService != null) {
            log.info("[TimerJobService.TimerJobService] stop now...");
            //立刻停止
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService = null;
        }
    }

    public void start(long delay,boolean isMaster) {
        this.isMaster = isMaster;
        if (null == scheduledExecutorService || scheduledExecutorService.isShutdown()) {
            scheduledExecutorService = create();
            log.info("[TimerJobService.TimerJobService] start...");
            startKeepAlive(delay);
        }
    }

    /**
     * 取消任务
     */
    public void restart(long delay,boolean isMaster) {
        stop();
        start(delay,isMaster);
    }



    /**
     * 更新最后一次访问时间
     */
    @Deprecated
    public void updateLastCallTime() {
        long millis = System.currentTimeMillis();
        log.debug("[KeepAliveService.updateLastCallTime] millis=" + TimeUtils.getTime(millis));

    }
}

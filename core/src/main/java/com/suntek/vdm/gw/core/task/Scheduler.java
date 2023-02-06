package com.suntek.vdm.gw.core.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
@Slf4j
public class Scheduler {
    private final ScheduledExecutorService scheduledThreadPool = new ScheduledThreadPoolExecutor(2,
            new ThreadFactoryBuilder().setNameFormat("gw-scheduler-%d").build());

    /**
     * 延时任务
     *
     * @return
     */
    public void scheduledThreadPoolLaterDo(long delay, TimeUnit unit, Runnable runnable) {
        scheduledThreadPool.schedule(runnable, delay, unit);
    }

    /**
     * 超时任务
     * @param delay
     * @param unit
     * @param runnable
     */
    public void scheduledThreadPoolTimeOut(long delay, TimeUnit unit, Runnable runnable){
        final Future handle = scheduledThreadPool.schedule(runnable, delay, unit);
        try {
            handle.get(4, TimeUnit.SECONDS);
        }catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
            handle.cancel(true);
        }finally {

        }

    }

    /**
     * 定时任务,
     * 1.任务一定要捕获异常
     * 2.任务的执行时间大于指定的间隔时间，将可能会发生，同一时间点 ，执行方法N 次
     *
     * @param initialDelay 任务创建后多久执行第一次
     * @param period       多久执行一次
     * @param unit         时间单位
     * @param runnable     任务
     */
    public void scheduledThreadPoolTimeDo(long initialDelay, long period, TimeUnit unit, Runnable runnable) {
        scheduledThreadPool.scheduleAtFixedRate(runnable, initialDelay, period, unit);
    }
}

package com.suntek.vdm.gw.core.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TaskManage {
    @Autowired
    private CronTaskRegistrar cronTaskRegistrar;

    public void remoteTokenKeepAliveStart() {
        add("timedTask-core", "remoteTokenKeepAlive", "*/45 * * * * ?");
    }

    public void remoteTokenKeepAliveStop() {
        del("timedTask-core", "remoteTokenKeepAlive");
    }

    public void localTokenCleanStart() {
        add("timedTask-core", "localTokenClean", "*/120 * * * * ?");
    }

    public void localTokenCleanStop() {
        del("timedTask-core", "localTokenClean");
    }


    public void remoteReLoginStart() {
        add("timedTask-core", "remoteReLogin", "*/15 * * * * ?");
    }

    public void remoteReLoginStop() {
        del("timedTask-core", "remoteReLogin");
    }

    public void stompCheckStart() {
        add("timedTask-conf", "stompCheck", "*/20 * * * * ?");
    }

    public void stompCheckStop() {
        del("timedTask-conf", "stompCheck");
    }

    public void internalKeepAliveStart() {
        add("timedTask-conf", "internalKeepAlive", "*/45 * * * * ?");
    }

    public void internalKeepAliveStop() {
        del("timedTask-conf", "internalKeepAlive");
    }


    private void add(String beanName, String methodName, String cronExpression) {
        //add(beanName, methodName, null, cronExpression);
    }

    private void add(String beanName, String methodName, String params, String cronExpression) {
        SchedulingRunnable task = new SchedulingRunnable(beanName, methodName, params);
        cronTaskRegistrar.addCronTask(task, cronExpression);
    }

    private void del(String beanName, String methodName) {
        del(beanName, methodName, null);
    }

    private void del(String beanName, String methodName, String params) {
        SchedulingRunnable task = new SchedulingRunnable(beanName, methodName, params);
        cronTaskRegistrar.removeCronTask(task);
    }
}

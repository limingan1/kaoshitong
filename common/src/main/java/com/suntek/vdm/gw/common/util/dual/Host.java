package com.suntek.vdm.gw.common.util.dual;


import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class Host {
    private static class Singleton {
        private static final Host INSTANCE = new Host();
    }

    public static Host getInstance() {
        return Singleton.INSTANCE;
    }

    public Host(){
        try {
            InetAddress addr = InetAddress.getLocalHost();
            masterHostName = addr.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private String masterHostName = "";
    private String slaveHostName = "";
    private Boolean isDual = false;
    private Boolean isMaster = false;

    public boolean isRunCmd() {
        return isRunCmd;
    }

    public void setRunCmd(boolean runCmd) {
        isRunCmd = runCmd;
    }

    private boolean isRunCmd = true;

    public Boolean getDual() {
        return isDual;
    }

    public void setDual(Boolean dual) {
        isDual = dual;
    }

    public Boolean getMaster() {
        return isMaster;
    }

    public void setMaster(Boolean master) {
        isMaster = master;
    }

    public String getMasterHostName() {
        return masterHostName;
    }

    public String getSlaveHostName() {
        return slaveHostName;
    }

    public void setSlaveHostName(String slaveHostName) {
        if(this.slaveHostName.isEmpty()){
            log.info("set slaveName to host:" + slaveHostName);
            this.slaveHostName = slaveHostName;
        }
    }
}

package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.conf.enumeration.SiteTypes;
import lombok.extern.slf4j.Slf4j;

/**
 * @author meshel
 */
@Slf4j
public class CasChannelInfo {
    private int index;
    private SiteTypes status = SiteTypes.ALLOCA_UINIT;
    private SiteTypes callStatus = SiteTypes.ALLOCA_UINIT;
    private long allocatTimeStamp = 0;
    private boolean lockStatus = false;
    private String owner="";
    private String destSource = "";
    private boolean ownerUsing = false;
    private String videoSource = "";
    private String oldVidoSource = "";

    public boolean setVideoSource(String strInitSource){
        if(this.videoSource.equals(strInitSource)){
            return false;
        }
        if(null == strInitSource){
            return true;
        }
        this.videoSource = strInitSource;
        return true;
    }

    public String getVideoSource(){
        return videoSource;
    }

    public String getOldVideoSource(){
        return oldVidoSource;
    }

    public CasChannelInfo(int index) {
        this.index = index;
    }

    public boolean isOwnerUsing() {
        return ownerUsing;
    }

    public void setOwnerUsing(boolean ownerUsing) {
        this.ownerUsing = ownerUsing;
    }


    public String getDestSource() {
        return destSource;
    }

    public SiteTypes getCallStatus() {
        return callStatus;
    }

    public long getAllocatTimeStamp() {
        return allocatTimeStamp;
    }

    public String getOwner() {
        return owner;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(int iIndex) {
        this.index = iIndex;
    }

    public boolean getLockStatus() {
        return lockStatus;
    }

    public void setLockStatus(boolean lockStatus) {
        if(SiteTypes.CALL_CONNECTED != callStatus){
            this.lockStatus = false;
            return;
        }
        this.lockStatus = lockStatus;
    }

    public void SetUsedFlag(String owner,String source,boolean useFlag){
        if(useFlag){
            allocatTimeStamp = System.currentTimeMillis();
            this.owner = owner;
            status = SiteTypes.ALLOCA_USED;
            this.destSource = source;
            ownerUsing = false;
            log.info("Allocat index "+index+" owner="+owner+" source"+source);
        }
        else{
            if(owner != null && !owner.isEmpty() && !this.owner.equals(owner)) {
                return;
            }
            if(SiteTypes.ALLOCA_UINIT == status){
                return;
            }
            allocatTimeStamp = 0;
            this.owner = "";
            this.destSource = "";
            status = SiteTypes.ALLOCA_FREE;
            ownerUsing = false;
            log.info("Free index "+index+" owner="+owner+" source"+source);
        }
    }

    public SiteTypes getStatus() {
        return status;
    }

    public SiteTypes updateCallInfo(SiteTypes callStatus){
        if(callStatus == this.callStatus){
            return SiteTypes.ACTION_NONE;
        }
        SiteTypes actonType = SiteTypes.ACTION_NONE;
        if(SiteTypes.CALL_CONNECTED == this.callStatus){
            if(!destSource.isEmpty()){
                oldVidoSource = destSource;
            }
            if(SiteTypes.ALLOCA_USED == status){
                SetUsedFlag(owner,"",false);
            }
            actonType = SiteTypes.ACTION_UNINIT;
            OnUninit();
        }
        else if(SiteTypes.CALL_CONNECTED == callStatus){
            status = SiteTypes.ALLOCA_FREE;
            actonType = SiteTypes.ACTION_INIT;
        }
        this.callStatus = callStatus;
        return actonType;
    }

    private void OnUninit(){
        status = SiteTypes.ALLOCA_UINIT;
        lockStatus = false;
        videoSource = "";
        allocatTimeStamp = 0;
        owner="";
        destSource = "";
        ownerUsing = false;
    }

    @Override
    public String toString() {
        return "CascadeChannelInfo{" +
                "index=" + index +
                ", status=" + status +
                ", callStatus=" + callStatus +
                ", allocatTimeStamp=" + allocatTimeStamp +
                ", lockStatus=" + lockStatus +
                ", owner='" + owner + '\'' +
                ", destSource='" + destSource + '\'' +
                ", ownerUsing=" + ownerUsing +
                ", videoSource='" + videoSource + '\'' +
                '}';
    }
}

package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.conf.enumeration.SiteTypes;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class MuitlCasChannel {
    private String directionName = "";
    private  static int masterIndex = 0;
    private  int lastAllocatIndex = 0;
    private int maxCascadeNum = 0;
    private Map<String,Integer> ownerMap = new HashMap<>(16);
    private  Map<String,Integer> sourceMap = new HashMap<>(16);
    private List<CasChannelInfo> ChannelLst = new ArrayList<>(64);
    MuitlCasChannel(Integer initNum,String directionName){
        this.directionName = directionName;
        //长度为0，补充初始1条
        if(initNum == 0){
            initNum = 1;
        }
        for(int j = 0; j< initNum;j++){
            CasChannelInfo cascadeChannelInfo = new CasChannelInfo(j);
            ChannelLst.add(cascadeChannelInfo);
        }
    }

    public Integer getMaxCascadeNum() {
        return maxCascadeNum;
    }

    public void setMaxCascadeNum(Integer maxCascadeNum) {
        this.maxCascadeNum = maxCascadeNum;
    }

    public List<CasChannelInfo> getChannelLst() {
        return ChannelLst;
    }


    /**
     *
     * @param owner 拥有者
     * @param isUseMaster 是否为master使用
     * @return null代表分配失败
     */
    public int allocate(String owner,String source,boolean isUseMaster){
        //寻找空闲位置，轮询查找
        log.info("allocate owner="+owner+" source="+source+" isUseMaster="+isUseMaster);
        CasChannelInfo cascadeChannelInfo;
        long lastAllocatTime = Long.MAX_VALUE;
        long chanelAllocatTime;
        int iOldestChannelIndex = -1;
        if(isUseMaster){
            cascadeChannelInfo = ChannelLst.get(masterIndex);
            log.info("cascadeChannelInfo" + cascadeChannelInfo);
            if(SiteTypes.CALL_CONNECTED != cascadeChannelInfo.getCallStatus()){
                return -1;
            }
            dealAllocatTable(cascadeChannelInfo,owner,source);
            cascadeChannelInfo.SetUsedFlag(owner,source,true);
            return cascadeChannelInfo.getIndex();
        }
        if(owner.isEmpty()){
            Integer iIndex = allocatBySource(owner,source);
            if(null != iIndex) {
                return iIndex;
            }
        }
        else{
            Integer iIndex = allocatByOwner(owner,source);
            if(null != iIndex) {
                return iIndex;
            }
        }
        int iMax = maxCascadeNum;
        if(0 == maxCascadeNum){
            iMax = ChannelLst.size();
        }
        for(int i = 1;i<iMax;i++){
            lastAllocatIndex++;
            if(lastAllocatIndex >=iMax){
                lastAllocatIndex = 1;
            }
            cascadeChannelInfo = ChannelLst.get(lastAllocatIndex);
            chanelAllocatTime = cascadeChannelInfo.getAllocatTimeStamp();
            if(chanelAllocatTime > 0 && chanelAllocatTime < lastAllocatTime){
                lastAllocatTime = chanelAllocatTime;
                iOldestChannelIndex = lastAllocatIndex;
            }
            if(SiteTypes.ALLOCA_FREE == cascadeChannelInfo.getStatus()){
                dealAllocatTable(cascadeChannelInfo,owner,source);
                cascadeChannelInfo.SetUsedFlag(owner,source,true);
                return cascadeChannelInfo.getIndex();
            }
        }
        if(!owner.isEmpty()){
            Integer iIndex = allocatBySource(owner,source);
            if(null != iIndex) {
                return iIndex;
            }
        }
        CasChannelInfo masterChannelInfo = ChannelLst.get(masterIndex);
        if(SiteTypes.ALLOCA_FREE == masterChannelInfo.getStatus()){
            dealAllocatTable(masterChannelInfo,owner,source);
            masterChannelInfo.SetUsedFlag(owner,source,true);
            return masterChannelInfo.getIndex();
        }
        chanelAllocatTime = masterChannelInfo.getAllocatTimeStamp();
        if(chanelAllocatTime > 0 && chanelAllocatTime < lastAllocatTime){
            iOldestChannelIndex = masterIndex;
        }
        if(iOldestChannelIndex >= 0){
            cascadeChannelInfo = ChannelLst.get(iOldestChannelIndex);
            dealAllocatTable(cascadeChannelInfo,owner,source);
            cascadeChannelInfo.SetUsedFlag(owner,source,true);
            return cascadeChannelInfo.getIndex();
        }
        return -1;
    }

    private Integer allocatBySource(String newOwner,String newSource){
        Integer iIndex = sourceMap.get(newSource);
        if(null == iIndex)
        {
            CasChannelInfo masterChannel = ChannelLst.get(0);
            if(masterChannel.getDestSource().equals(newSource)){
                dealAllocatTable(masterChannel,newOwner,newSource);
                return 0;
            }
            return null;
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(iIndex);
        dealAllocatTable(cascadeChannelInfo,newOwner,newSource);
        cascadeChannelInfo.SetUsedFlag(newOwner,newSource,true);
        return iIndex;
    }

    private Integer allocatByOwner(String newOwner,String newSource) {
        Integer iIndex = ownerMap.get(newOwner);
        if(null == iIndex) {
            return iIndex;
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(iIndex);
        cascadeChannelInfo.SetUsedFlag(newOwner,newSource,true);
        return iIndex;
    }

    private void dealAllocatTable(CasChannelInfo oChannel, String newOwner, String newSource){
        replaceTable(ownerMap,oChannel.getOwner(),newOwner,oChannel.getIndex());
        replaceTable(sourceMap,oChannel.getDestSource(),newSource,oChannel.getIndex());
    }

    public  void replaceTable(Map<String,Integer> map,String strOld,String strNew,int iIndex){
        if(strOld.equals(strNew) || strNew.isEmpty()) {
            return;
        }
        if(!strOld.isEmpty()){
            map.remove(strOld);
        }
        map.put(strNew,iIndex);
    }
    /**
     *
     * @param owner
     * @return null代表释放失败
     */
    public void deallocate(String owner){

        Integer iIndex = ownerMap.get(owner);
        if(null == iIndex) {
            return;
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(iIndex);
        ownerMap.remove(owner);
        String strSource = cascadeChannelInfo.getDestSource();
        if(!strSource.isEmpty()){
            sourceMap.remove(strSource);
        }
        cascadeChannelInfo.SetUsedFlag(owner,"",false);
    }

    //取得currentNum
    public Integer getCurrentNum() {
        Integer tempCurrent = 0;
        CasChannelInfo cascadeChannelInfo;
        for(int i=0;i<maxCascadeNum;i++){
            cascadeChannelInfo = ChannelLst.get(i);
            SiteTypes siteTypes = cascadeChannelInfo.getCallStatus();
            if(siteTypes != SiteTypes.ALLOCA_UINIT){
                tempCurrent++;
            }
        }

        if(0 == tempCurrent) {
            return 1;
        }
        return tempCurrent;
    }

    public void onSiteSourceChanged(String strUri,String strVideoSource,int iIndex){
        Integer iAllocatIndex = ownerMap.get(strUri);
        if(null == iAllocatIndex){
            return;
        }
        CasChannelInfo oldChannel = ChannelLst.get(iAllocatIndex);
        if(iIndex < 0 || iIndex > ChannelLst.size())
        {
            if(oldChannel.isOwnerUsing()){
                deallocate(strUri);
                return;
            }
            return;
        }
        if(iIndex != iAllocatIndex){
            if(oldChannel.isOwnerUsing()){
                deallocate(strUri);
                return;
            }
            return;
        }
        if(!oldChannel.isOwnerUsing()){
            oldChannel.setOwnerUsing(true);
        }
    }
    public boolean isReady(int index){
        if(index < 0 || index > ChannelLst.size()) {
            return false;
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(index);
        if(SiteTypes.CALL_CONNECTED != cascadeChannelInfo.getCallStatus()){
            return false;
        }
        return true;
    }

    public SiteTypes updateChannelInfo(SiteTypes siteTypes,Integer index){
        if(index < 0 || index > ChannelLst.size()) {
            return SiteTypes.ACTION_NONE;
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(index);
        String oldOwner = cascadeChannelInfo.getOwner();
        SiteTypes actionType = cascadeChannelInfo.updateCallInfo(siteTypes);
        if(!oldOwner.isEmpty() && actionType==SiteTypes.ACTION_UNINIT) {
            ownerMap.remove(oldOwner);
        }
        return actionType;
    }

    public boolean getChannelLockStatus(int index){
        if(index < 0 || index > ChannelLst.size()) {
            return false;
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(index);
        return cascadeChannelInfo.getLockStatus();
    }

    public void setChannelLockStatus(int index,boolean bLock){
        if(index < 0 || index > ChannelLst.size()) {
            return;
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(index);
        cascadeChannelInfo.setLockStatus(bLock);
    }

    public boolean setChannelVideoSource(int index,String videoSource){
        if(index < 0 || index > ChannelLst.size()) {
            return false;
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(index);
        return cascadeChannelInfo.setVideoSource(videoSource);
    }


    public String getChannelVideoSource(int index){
        if(index < 0 || index > ChannelLst.size()) {
            return "";
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(index);
        return cascadeChannelInfo.getVideoSource();
    }
    public String getChannelOldVideoSource(int index){
        if(index < 0 || index > ChannelLst.size()) {
            return "";
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(index);
        return cascadeChannelInfo.getOldVideoSource();
    }


    public boolean getReady(int index){
        if(index < 0 || index > ChannelLst.size()) {
            return false;
        }
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(index);
        if(SiteTypes.CALL_CONNECTED != cascadeChannelInfo.getCallStatus()){
            return false;
        }
        return true;
    }

    public boolean getDoing(int index){
        CasChannelInfo cascadeChannelInfo = ChannelLst.get(index);
        log.info(Thread.currentThread().getName() + "---" + System.currentTimeMillis()+" "+index+" getDoing callStatus: "+cascadeChannelInfo.getCallStatus());
        if(SiteTypes.ALLOCA_UINIT == cascadeChannelInfo.getCallStatus()){
            return true;
        }
        return false;
    }
}

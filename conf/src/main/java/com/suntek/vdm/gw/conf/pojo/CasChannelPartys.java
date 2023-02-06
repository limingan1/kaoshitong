package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.conf.enumeration.SiteTypes;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CasChannelPartys {
    private static Integer falseNum = -1;
    private static String nameSuffix = "(1)";
    private Integer currentNum = 0;
    private MuitlCasChannel muitlCasChannelUp = new MuitlCasChannel(64,"UP");
    private Map<String, MuitlCasChannel> lowMuitlCasChannelMap = new ConcurrentHashMap<>();

    private MuitlCasChannel getMuitlCasChannelLower(String casConfId){
        if(!lowMuitlCasChannelMap.containsKey(casConfId)){
            MuitlCasChannel muitlCasChannelLower= new MuitlCasChannel(64,"LOW");
            lowMuitlCasChannelMap.put(casConfId, muitlCasChannelLower);
            return muitlCasChannelLower;
        }
        return lowMuitlCasChannelMap.get(casConfId);
    }

    public synchronized void updateChannelLstInfo(String casConfId, CascadeParticipantDirection direction, SiteTypes siteTypes, MultiPicInfo cascadeVideoSource, Integer index){
        SiteTypes actionType;
        if(CascadeParticipantDirection.DOWN.equals(direction)){
            MuitlCasChannel muitlCasChannelLower = getMuitlCasChannelLower(casConfId);
            actionType =  muitlCasChannelLower.updateChannelInfo(siteTypes,index);
//            String strSmcId = welinkConf.getSmcConfID();
//            String strWelinkAccess = welinkConf.getAccessCode();
            String strUri ="";
            if (actionType == SiteTypes.ACTION_INIT) {
                //TODO 级联通道初始化
//                strUri = CascadeChannelParser.getSmcUriByNumber(strWelinkAccess,index);
//                boolean bNeedLock =  (!bMaster && !getAndSetChannelLockStatus(direction,index));
//                boolean bBroacdCast = (bMaster && !welinkConf.getStrBroadcastUri().isEmpty());
//                if(bBroacdCast){
//                    logger.info("[updateChannelLstInfo] broadcast :"+welinkConf.getStrBroadcastUri());
//                }
//                ConferenceService.getInstance().InitCasChannel(logMsg,strSmcId,strUri,
//                        bNeedLock,bMaster,bBroacdCast,welinkConf);
            }
            //是否设置单个通道视频源
//            InitLowerVideoSource(welinkConf,cascadeVideoSource, index);
            return;
        }
        if(CascadeParticipantDirection.UP.equals(direction)) {
            actionType = muitlCasChannelUp.updateChannelInfo(siteTypes, index);
            if (actionType == SiteTypes.ACTION_INIT) {
//                String strSmcAccessCode = welinkConf.getSmcAccessCode();
//                Integer iMaxCasNumber = muitlCasChannelUp.getMaxCascadeNum();
//                String strUri = CascadeChannelParser.getWelinkUriByNumber(strSmcAccessCode,index, iMaxCasNumber);
//                boolean bNeedLock = !getAndSetChannelLockStatus(direction,index);
//                WelinkService.getInstance().InitCasChannel(logMsg,welinkConf,strUri,muitlCasChannelUp,bNeedLock,bMaster,bMaster);
            }
            return;
        }
    }
    //分配
    public synchronized int allocate(String casConfId, CascadeParticipantDirection direction,String owner,String source,boolean isMaster){

        if(CascadeParticipantDirection.DOWN.equals(direction)){
            MuitlCasChannel muitlCasChannelLower = lowMuitlCasChannelMap.get(casConfId);
            if(muitlCasChannelLower == null){
                return -1;
            }
            return muitlCasChannelLower.allocate(owner,source,isMaster);
        }
        if(CascadeParticipantDirection.UP.equals(direction)){
            return muitlCasChannelUp.allocate(owner,source,isMaster);
        }
        return -1;
    }

    //释放
    public synchronized void deAllocate(String casConfId, CascadeParticipantDirection direction, String owner){
        if(CascadeParticipantDirection.DOWN.equals(direction)){
            MuitlCasChannel muitlCasChannelLower = lowMuitlCasChannelMap.get(casConfId);
            if(muitlCasChannelLower == null){
                return;
            }
            muitlCasChannelLower.deallocate(owner);
            return;
        }
        if(CascadeParticipantDirection.UP.equals(direction)){
            muitlCasChannelUp.deallocate(owner);
            return;
        }
    }

    public boolean getReady(String casConfId, CascadeParticipantDirection direction, int index){
        if(direction.equals(CascadeParticipantDirection.UP)){
            return muitlCasChannelUp.getReady(index);
        }
        else if (direction.equals(CascadeParticipantDirection.DOWN)){
            MuitlCasChannel muitlCasChannelLower = lowMuitlCasChannelMap.get(casConfId);
            if(muitlCasChannelLower == null){
                return false;
            }
            return muitlCasChannelLower.getReady(index);
        }
        return false;
    }

}

package com.suntek.vdm.gw.welink.util;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CasChannelPartyUtil {
    private Integer currentNum = 0;
    public synchronized Map<String ,Integer> getLeftAddCount(Integer onlineSiteSize,int currentOnlineNum,int maxCascadeNum){
        int falseNum = -1;
        Map<String ,Integer> numResult = new HashMap<>();
        int resultNum;
        currentNum = currentOnlineNum;
        log.info("getLeftAddCount -> : currentNum:"+currentNum+",onlineSiteSize:"+onlineSiteSize+",maxCascadeNum:"+maxCascadeNum);
        if(currentNum >= maxCascadeNum){
            numResult.put("resultNum", falseNum);
            return numResult;
        }
        if(currentNum >= onlineSiteSize){
            numResult.put("resultNum", falseNum);
            return numResult;
        }
        int maxNum= Math.min(onlineSiteSize,maxCascadeNum);
        resultNum = maxNum - currentNum;
        log.info("CasChannelParty not enough need to append "+resultNum+" currNum="+currentNum+" maxNum="+maxCascadeNum+" onlineSiteSize="+onlineSiteSize);
        int startNum = currentNum;
        currentNum += resultNum;
        log.info("currentNum update to : " + currentNum);
        numResult.put("resultNum", resultNum);
        numResult.put("currentNum", startNum);
        return numResult;
    }

}

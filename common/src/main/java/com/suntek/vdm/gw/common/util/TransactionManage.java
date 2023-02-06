package com.suntek.vdm.gw.common.util;

import com.suntek.vdm.gw.common.pojo.TransactionId;
import com.suntek.vdm.gw.common.pojo.TransactionManageInfo;
import com.suntek.vdm.gw.common.pojo.TransactionType;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class TransactionManage {
    private static Map<TransactionId, TransactionManageInfo> map = new ConcurrentHashMap<>();

    public static Map<TransactionId, TransactionManageInfo> getMap() {
        if (map == null) {
            map = new ConcurrentHashMap<>();
        }
        return map;
    }

    public static TransactionManageInfo get(TransactionId tranId) {
        if (getMap().containsKey(tranId)) {
            return getMap().get(tranId);
        }
        return null;
    }

    public static void wait(TransactionId tranId) {
        wait(tranId, 0);
    }

    public static void wait(TransactionId tranId, long timeout) {
        TransactionManageInfo info = get(tranId);
        if (info == null) {
            info = new TransactionManageInfo();
            getMap().put(tranId, info);
        }
        if (info.flag!=null&&info.flag){
            log.info("Transaction wait by  tranId:{} is flag",tranId.toString());
            return;
        }
        try {
            log.info("Transaction wait by  tranId:{}",tranId.toString());
            synchronized(info){
                info.wait(timeout);
            }
        } catch (InterruptedException e) {
            log.info("Transaction wait exception tranId:{} error:{}", tranId.toString(), e.getMessage());
        }
    }


    public static void notify(TransactionId tranId) {
        TransactionManageInfo info = get(tranId);
        if (info == null) {
            info = new TransactionManageInfo();
            getMap().put(tranId, info);
        }
        log.info("Transaction notify by tranId:{}",tranId.toString());
        synchronized(info){
            info.flag=true;
            //需要通知全部
            info.notifyAll();
        }
        //通知不要删除 中间的会议需要两次通知
        if(TransactionType.WELINK_TOKEN.equals(tranId.getType()) || TransactionType.WEBSOCKET.equals(tranId.getType())){
            getMap().remove(tranId);
        }
        //getMap().remove(tranId);
    }
    public static void clean(TransactionId tranId) {
        getMap().remove(tranId);
    }

    public static void clean() {
        Iterator<Map.Entry<TransactionId, TransactionManageInfo>> it = getMap().entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<TransactionId, TransactionManageInfo> entry=it.next();
            if (entry.getValue().expired()){
                log.info("Transaction expired by tranId {}",entry.getKey().toString());
                it.remove();
            }
        }
    }
}

package com.suntek.vdm.gw.core.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CommonCache {



    /**
     * 用户明  token tickets 对应关系
     */
    private static Map<String, String> CONFERENCES_TOKEN=new ConcurrentHashMap<>();

    public static Map<String, String> getConferencesToken() {
        if (CONFERENCES_TOKEN==null){
            CONFERENCES_TOKEN=new ConcurrentHashMap<>();
        }
        return CONFERENCES_TOKEN;
    }

    /**
     * 用户明  token tickets 对应关系
     */
    private static Map<String, Map<String, String>> USER_TICKETS_MAP;


    //TODO 缓存清理需要实现
    public static Map<String, Map<String, String>> getUserTicketsMap() {
        if (USER_TICKETS_MAP == null) {
            USER_TICKETS_MAP = new ConcurrentHashMap<>();
        }
        return USER_TICKETS_MAP;
    }


    /**
     *系统加载状态(初始化)
     */
    public static boolean LOAD_STATUS=false;
}

package com.suntek.vdm.gw.conf.enumeration;

/**
 * @author meshel
 */

public enum SiteTypes {
    ALLOCA_UINIT(0),
    ALLOCA_USED(1),
    ALLOCA_FREE(2),
    CALL_CALLING(3),
    CALL_CONNECTING(4),
    CALL_CONNECTED(5),
    CALL_DISCONNECTED(6),
    ACTION_NONE(7),
    ACTION_UNINIT(8),
    ACTION_INIT(9);

    private Integer value;
    SiteTypes(Integer value){
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public static SiteTypes getValueFromString(String strValue,String videoOpen,boolean bIsMaster){
        SiteTypes callStatus = SiteTypes.CALL_DISCONNECTED;
        if("Connecting".equals(strValue)){
            callStatus = SiteTypes.CALL_CONNECTING;
        }else if("Connected".equals(strValue) && (bIsMaster || ("true".equals(videoOpen)))) {
            callStatus = SiteTypes.CALL_CONNECTED;
        }
        return callStatus;
    }
    public static SiteTypes checkSiteType(Integer typeValue){
        for(SiteTypes e : SiteTypes.values()){
            if(e.getValue().equals(typeValue)){
                return e;
            }
        }
        return ALLOCA_UINIT;
    }
}

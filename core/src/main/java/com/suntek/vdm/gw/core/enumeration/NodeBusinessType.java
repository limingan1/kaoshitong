package com.suntek.vdm.gw.core.enumeration;

public enum NodeBusinessType {
    SMC(1),
    WELINK(2),
    CLOUDLINK(3);
    private int value = 0;

    NodeBusinessType(int value) {     //必须是private的，否则编译错误
        this.value = value;
    }

    public static NodeBusinessType valueOf(int value) {
        switch (value) {
            case 1:
                return SMC;
            case 2:
                return WELINK;
            case 3:
                return CLOUDLINK;
            default:
                return SMC;
        }
    }

    public int value() {
        return this.value;
    }

    public static boolean isWelinkOrCloudLink(Integer type){
        if (type == null) {
            return false;
        }
        return WELINK.value == type || CLOUDLINK.value == type;
    }
    public static boolean isWelinkOrCloudLink(NodeBusinessType type){
        if (type == null) {
            return false;
        }
        return WELINK.value == type.value || CLOUDLINK.value == type.value;
    }
}

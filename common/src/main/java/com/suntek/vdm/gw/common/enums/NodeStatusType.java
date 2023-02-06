package com.suntek.vdm.gw.common.enums;

public enum NodeStatusType {
    ONLINE(1),
    OFFLINE(0);
    private int value = 0;

    private NodeStatusType(int value) {     //必须是private的，否则编译错误
        this.value = value;
    }

    public int toValue() {
        return ordinal();
    }


    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static NodeStatusType valueOf(int value) {
        switch (value) {
            case 1:
                return ONLINE;
            case 0:
                return OFFLINE;
            default:
                return null;
        }
    }
    public int value() {
        return value;
    }
    public int get() {
        return value;
    }
}

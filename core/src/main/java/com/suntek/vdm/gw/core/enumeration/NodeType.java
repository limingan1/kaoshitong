package com.suntek.vdm.gw.core.enumeration;

public enum NodeType {
    TOP(1),
    THIS(0),
    LOW(-1);
    private int value = 0;




    NodeType(int value) {     //必须是private的，否则编译错误
        this.value = value;
    }
    public static NodeType valueOf(int value) {
        switch (value) {
            case 1:
                return TOP;
            case 0:
                return THIS;
            case -1:
                return LOW;
            default:
                return null;
        }
    }
    public int value() {
        return this.value;
    }

    public static boolean isRemoteNode(Integer value) {
        return !(NodeType.THIS.value == value);
    }
}

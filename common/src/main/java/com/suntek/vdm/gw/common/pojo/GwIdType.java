package com.suntek.vdm.gw.common.pojo;

public enum GwIdType {
    V1("00000000000000000000000000000000"),
    V2("00000000000000001111111111111111"),
    V3("other");
    private String value;

    GwIdType(String value) {     //必须是private的，否则编译错误
        this.value = value;
    }


    public static GwIdType get(String value) {
        switch (value) {
            case "00000000000000000000000000000000":
                return V1;
            case "00000000000000001111111111111111":
                return V2;
            default:
                return V3;
        }
    }

    public String value() {
        return this.value;
    }
}

package com.suntek.vdm.gw.common.enums;

public enum SmcVersionType {
    V2("2.0"),
    V3("3.0"),
    Welink("welink");

    private String value;

    SmcVersionType(String value) {
        this.value = value;
    }



    public static SmcVersionType valueOfString(String value) {
        switch (value) {
            case "2.0":
                return V2;
            case "3.0":
                return V3;
            case "welink":
                return Welink;
            default:
                return null;
        }
    }


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

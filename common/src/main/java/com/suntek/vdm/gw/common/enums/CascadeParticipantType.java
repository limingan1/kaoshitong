package com.suntek.vdm.gw.common.enums;

public enum CascadeParticipantType {
    H323(0),
    SIP(1);

    private Integer value;

    CascadeParticipantType(Integer value) {
        this.value = value;
    }

    public static CascadeParticipantType valueOf(int value) {
        switch (value) {
            case 0:
                return H323;
            case 1:
                return SIP;
            default:
                return null;
        }
    }


    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

}

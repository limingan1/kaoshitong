package com.suntek.vdm.gw.common.enums;

public enum CascadeParticipantDirection {
    UP(0),
    DOWN(1);

    private Integer value;

    CascadeParticipantDirection(Integer value) {
        this.value = value;
    }

    public static CascadeParticipantDirection valueOf(int value) {
        switch (value) {
            case 0:
                return UP;
            case 1:
                return DOWN;
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

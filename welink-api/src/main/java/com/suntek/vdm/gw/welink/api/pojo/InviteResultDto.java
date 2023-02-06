package com.suntek.vdm.gw.welink.api.pojo;

import java.io.Serializable;

public class InviteResultDto implements Serializable {

    private String callNumber;

    private String resultCode;

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    @Override
    public String toString() {
        return "InviteResultDto{" +
                "callerNumber='" + callNumber + '\'' +
                ", resultCode='" + resultCode + '\'' +
                '}';
    }
}

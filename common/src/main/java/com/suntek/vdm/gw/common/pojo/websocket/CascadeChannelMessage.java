package com.suntek.vdm.gw.common.pojo.websocket;

import lombok.Data;

@Data
public class CascadeChannelMessage {
    private String smcAccessCode;
    private String welinkAccessCode;
    private String nodeName;
    private Integer cascadeNum;
    private Integer currentTotalNum;
    private String guestPwd;
    private String token;

    public CascadeChannelMessage(String smcAccessCode, String welinkAccessCode, String nodeName, Integer cascadeNum, Integer currentTotalNum, String guestPwd) {
        this.smcAccessCode = smcAccessCode;
        this.welinkAccessCode = welinkAccessCode;
        this.nodeName = nodeName;
        this.cascadeNum = cascadeNum;
        this.currentTotalNum = currentTotalNum;
        this.guestPwd = guestPwd;
    }
}

package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class DelAttendInfo {
    /**
     * 会议标识。
     */
    private String number;

    /**
     * 与会者标识，已入会的 必须填写该字段。
     */
    private String participantID;
}
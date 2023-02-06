package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RealTimeAttendee {
    /**
     * 与会者帐号。
     */
    private String accountID;

    /**
     * 与会者的用户uuid。
     */
    private String userUUID;

    /**
     * 与会者名称或昵称，长度限制为96个字 符。
     */
    private String name;

    /**
     * 与会者设备的注册号码（可支持SIP、 TEL号码格式）。最大不超过127个字 符。
     * 设备为三屏智真时的中屏号码（三屏智 真为预留接口）。
     */
    private String phone;

    /**
     * 设备为三屏智真时的左屏号码（预留接 口）。
     */
    private String phoneLeft;

    /**
     * 设备为三屏智真时的右屏号码（预留接 口）。
     */
    private String phoneRight;
}
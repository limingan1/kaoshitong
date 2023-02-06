package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class PartAttendee {
    /**
     * 与会者名称或昵称。长度 限制为96个字符。
     */
    private String name;

    /**
     * 电话号码（可支持SIP、 TEL号码格式）。最大不 超过127个字符。phone、 email和sms三者需至少填 写一个。
     * 说明
     * 当“type”为
     * “telepresence”时，且设 备为三屏智真，则该字段填 写中屏号码。（三屏智真为 预留字段）
     */
    private String phone;

    /**
     * 电话号码（可支持SIP、 TEL号码格式）。最大不 超过127个字符。phone、 email和sms三者需至少填 写一个。
     * 说明
     * 当“type”为
     * “telepresence”时，且设 备为三屏智真，则该字段填 写中屏号码。（三屏智真为 预留字段）
     */
    private String phone2;

    /**
     * 电话号码（可支持SIP、 TEL号码格式）。最大不 超过127个字符。phone、 email和sms三者需至少填 写一个。
     * 说明
     * 当“type”为
     * “telepresence”时，且设 备为三屏智真，则该字段填 写中屏号码。（三屏智真为 预留字段）
     */
    private String phone3;

    /**
     * 电话号码（可支持SIP、 TEL号码格式）。最大不 超过127个字符。phone、 email和sms三者需至少填 写一个。
     * 说明
     * 当“type”为
     * “telepresence”时，且设 备为三屏智真，则该字段填 写中屏号码。（三屏智真为 预留字段）
     */
    private String type;
}
package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RealTimeParticipant {
    /**
     * 与会者的用户uuid。
     */
    private String pid;

    /**
     * 与会者名称或昵称，长度限制为96个字 符。
     */
    private String name;

    /**
     * 与会者设备的注册号码（可支持SIP、 TEL号码格式）。最大不超过127个字 符。
     */
    private String phone;

    /**
     * hand	Integer	与会者举手状态。 ● 0：未举手 ● 1：举手
     */
    private Integer state;

    /**
     * hand	Integer	与会者举手状态。 ● 0：未举手 ● 1：举手
     */
    private Integer video;

    /**
     * 麦克风状态。
     * ● 0：麦克风打开
     * ● 1：麦克风关闭
     */
    private Integer mute;

    /**
     * 与会者举手状态。 ● 0：未举手 ● 1：举手
     */
    private Integer hand;
}
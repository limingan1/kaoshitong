package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class AttendeeRsp {
    /**
     * 与会人ID
     */
    private String id;
    private String uri;

    /**
     * 与会者名称
     */
    private String name;

    /**
     * 与会者账号名称
     */
    private String account;

    /**
     * 会场名称
     */
    private String participantName;

    /**
     * 邮箱地址
     */
    private String email;

    /**
     * 组织名称
     */
    private String organizationName;

    /**
     * 与会人电话号码
     */
    private String mobile;

    /**
     * 签到时间
     */
    private String checkInTime;

    /**
     * 签到类型
     */
    private String checkInType;
}
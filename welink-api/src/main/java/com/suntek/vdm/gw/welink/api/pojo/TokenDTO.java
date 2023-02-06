package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class TokenDTO {
    /**
     * 接入Token字符串。
     */
    private String accessToken;

    /**
     * 用户IP。
     */
    private String tokenIp;

    /**
     * Token的有效时长，单位：秒。
     */
    private Integer validPeriod;

    /**
     * Token的失效时间戳，单位：秒。
     */
    private Integer expireTime;

    /**
     * 用户鉴权信息。
     */
    private UserInfo user;

    /**
     * 登录帐号类型。
     * ● 0：Web客户端类型
     * ● 5：cloudlink pc
     * ● 6：cloudlink mobile
     * ● 16：workplace pc
     */
    private Integer clientType;

    /**
     * 抢占登录标识。
     * ● 0：非抢占
     * ● 1：抢占（未启用）
     */
    private Integer forceLoginInd;

    /**
     * 是否首次登录。
     * 说明
     * 首次登录表示尚未修改过密码。首次登录
     * 时，系统会提醒用户需要修改密码。
     * 默认值：false。
     */
    private Boolean firstLogin;

    /**
     * 密码是否过期。
     * 默认值：false。
     */
    private Boolean pwdExpired;

    /**
     * 密码有效天数。
     */
    private Integer daysPwdAvailable;
}
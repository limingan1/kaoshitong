package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class AuthProxyInfo {

    /**
     * 鉴权服务类型，如workplace
     */
    private String authServerType;
    /**
     * 鉴权类型：AccountAndPwd：用户名+密码登录模式 LongTicket：长Token票据登录模式 ShortTicket：短Token票据登录模式 AuthCode：授权码登录模式
     */
    private String authType;
    /**
     * 鉴权token
     */
    private String credential;
    /**
     *认证帐号
     */
    private String account;
    /**
     * 认证密码
     */
    private String pwd;
    /**
     * 登录帐号类型。 0：Web客户端类型； 5：cloudlink pc； 6：cloudlink mobile； 16：workplace pc； 18：workplace mobile
     */
    private Integer clientType;
    /**
     * 企业域名
     */
    private String domain;
    /**
     * 备注信息
     */
    private String remark;
    /**
     * 是否生成token。 0：生成，用于登录鉴权； 1：不生成。
     */
    private Integer createTokenType;
}

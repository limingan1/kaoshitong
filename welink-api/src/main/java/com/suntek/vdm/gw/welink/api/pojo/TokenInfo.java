package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class TokenInfo {
    /**
     * 会控鉴权Token。
     */
    private String token;

    /**
     * websocket建链鉴权Token，成功时必带。
     */
    private String tmpWsToken;

    /**
     * websocket建链URL。
     */
    private String wsURL;

    /**
     * 角色
     */
    private Integer role;

    /**
     * 会话过期时间。UTC时间毫秒数。
     */
    private Long expireTime;

    /**
     * 会议预定人ID。
     */
    private String userID;

    /**
     * 会议所属企业ID。
     */
    private String orgID;

    /**
     * 终端请求时，返回终端入会后会场ID。
     */
    private String participantID;

    /**
     * 会控token失效的时间。（单位秒）
     */
    private Integer confTokenExpireTime;
}
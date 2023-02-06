package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class TemplateParticipantReq {
    /**
     * 会场号码(127位以内的字符,H323:号码、SIP:号码、H323ANDSIP:号码 ， 号码为纯数字，最长32位)
     */
    private String uri;

    /**
     * 与会者名称(1~64字符)
     */
    private String name;

    /**
     * 邮箱地址（可选）
     */
    private String email;

    /**
     * 终端类型(1~32字符)（可选）
     */
    private Integer terminalType;

    /**
     * 通信协议（可选）
     */
    private Integer ipProtocolType;

    /**
     * 拨入拨出模式（可选）
     */
    private String dialMode;

    /**
     * 会场加密方式（可选）
     */
    private String encodeType;

    /**
     * 是否纯转发（可选）
     */
    private Boolean forward;

    /**
     * 组织名称(1~64字符)（可选）
     */
    private String organizationName;

    /**
     * 会场速率
     */
    private Integer rate;

    /**
     * 服务区ID（可选）
     */
    private String serviceZoneId;

    /**
     * 音频协议（可选）
     */
    private Integer audioProtocol;

    /**
     * 视频协议（可选）
     */
    private Integer videoProtocol;

    /**
     * 分辨率（可选）
     */
    private Integer videoResolution;

    /**
     * 共享能力（可选）
     */
    private Integer dataConfProtocol;

    /**
     * 是否主会场（可选）
     */
    private Boolean mainParticipant;

    /**
     * 二次拨号
     */
    private String dtmfInfo;
}
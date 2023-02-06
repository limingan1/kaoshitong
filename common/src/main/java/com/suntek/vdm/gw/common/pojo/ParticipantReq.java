package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class ParticipantReq {

    /**
     * 会场号码(127位以内的字符,H323:号码、SIP:号码、H323ANDSIP:号码，号码为纯数字，最长32位)
     */
    private String uri;

    /**
     * 与会者名称(1~64字符)
     */
    private String name;

    /**
     * 邮箱地址(可选)
     */
    private String email;

    /**
     * 终端类型(1~32字符)(可选)
     */
    private String terminalType;

    /**
     * 通信协议(可选)  0 H323   1 SIP    2 双协议
     */
    private Integer ipProtocolType;

    /**
     * 拨入拨出模式(可选)
     */
    private String dialMode;

    /**
     * 会场加密方式(可选)
     */
    private String encodeType;

    /**
     * 是否纯转发(可选)
     */
    private Boolean forward;

    /**
     * 组织名称(1~64字符)(可选)
     */
    private String organizationName;

    /**
     * 是否语音会场(可选)
     */
    private Boolean voice;

    /**
     * 会场速率
     */
    private Number rate;

    /**
     * 服务区ID(可选)
     */
    private String serviceZoneId;

    /**
     * 音频协议(可选)
     */
    private Integer audioProtocol;

    /**
     * 视频协议(可选)
     */
    private Integer videoProtocol;

    /**
     * 分辨率(可选)
     */
    private Integer videoResolution;

    /**
     * 共享能力(0 标清 1高清)(可选)
     */
    private Integer dataConfProtocol;

    /**
     * MCUId
     */
    private String mcuId;

    /**
     * MCU名称
     */
    private String mcuName;

    /**
     * 是否主会场(可选)
     */
    private Boolean mainParticipant;

    /**
     * 二次拨号
     */
    private String dtmfInfo;

    /**
     * 备份会场参数
     */
    private BackupParticipant backupParticipant;

    /**
     * VDC备用使用长度64字符
     */
    private String vdcMarkCascadeParticipant;


    /**
     * MCU编码时不叠加会场名（true-不叠加; false-叠加；可选，默认false）
     */
    private Boolean notEncodeSiteName;

    /**
     * 会场是不协商辅流（true-不协商；false-协商 可选，默认false）
     */
    private Boolean disableAux;

    /**
     * 是否会场音频特殊处理，不混音、只编静音码流（true-不混音；false-混音；可选，默认false）
     */
    private Boolean notAudioMixing;

    /**
     * 会场不加入自动多画面（ true-不加入自动多画面；false-加入自动多画面； 可选，默认false）
     */
    private Boolean notJoinAutoMultiPic;

    /**
     * 指定主叫会场号码(可选)
     */
    private String specificCallerAlias;

    /**
     * 预置视频源锁定（可选）
     */
    private Boolean lockVideoSrc;

    /**
     * 会场不在终端侧会场列表中呈现（也包含SMC页面不呈现 true-不显示；false显示；默认false）
     */
    private Boolean notDisplay;

    /**
     * 与会类型 2.0专用专用补充
     */
    private String participantType;

    private String vmrNumber;

    private String leftUri;

    private String rightUri;

    /**
     * 备份会场信息
     */
    private String backupMiddleUri;

    private String backupName;

    private String backupTerminalType;

}
package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class TemplateParticipantRsp {
/**
*与会者ID
*/
private String id;

/**
*会场号码
*/
private String uri;

/**
*与会者名称
*/
private String name;

/**
*邮箱地址
*/
private String email;

/**
*终端类型
*/
private String terminalType;

/**
*通信协议
*/
private int ipProtocolType;

/**
*拨入拨出模式
*/
private String dialMode;

/**
*会场加密方式
*/
private String encodeType;

/**
*是否纯转发
*/
private Boolean forward;

/**
*组织名称
*/
private String organizationName;

/**
*MCUId
*/
private String mcuId;

/**
*MCU名称
*/
private String mcuName;

/**
*是否语音会场
*/
private Boolean voice;

/**
*会场速率
*/
private Number rate;

/**
*服务区ID
*/
private String serviceZoneId;

/**
*服务区名称
*/
private String serviceZoneName;

/**
*音频协议
*/
private Integer audioProtocol;

/**
*视频协议
*/
private Integer videoProtocol;

/**
*分辨率
*/
private Integer videoResolution;

/**
*共享能力
*/
private Integer dataConfProtocol;

/**
*是否主会场
*/
private Boolean mainParticipant;

/**
*TP参数
*/
private TpParamDto tpParam;

    /**
     * 二次拨号
     */
    private String dtmfInfo;
}
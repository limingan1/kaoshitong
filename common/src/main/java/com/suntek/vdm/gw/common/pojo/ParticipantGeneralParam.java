package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ParticipantGeneralParam {
    /**
     * 会场Id
     */
    private String id;

    /**
     * 会场名称
     */
    private String name;

    /**
     * 会场标识
     */
    private String uri;

    /**
     * 会场类型
     */
    private int type;

    /**
     * 是否语音会场
     */
    private Boolean voice;

    /**
     * 会场设备类型
     */
    private int model;

    /**
     * 代理通道ID
     */
    private String proxyId;

    /**
     * 级联通道时携带，表示对端级联所在会议Id
     */
    private String remoteConfId;

    /**
     * 会场的编解码模式
     */
    private String encodeType;

    /**
     * 终端侧是否显示
     */
    private Boolean display;

    /**
     * 会场token
     */
    private String token;

    /**
     * 二次拨号信息
     */
    private String dtmfInfo;

    /**
     * 备份会场
     */
    private BackupParticipant backupParticipant;

    /**
     * 码流编解码技术
     */
    private String participantMediaType;

    /**
     * 是否本级会场
     */
    private boolean local;

    /**
     * Tp子屏信息
     */
    private List<TpGeneralParam> subTpParams;

    /**
     * mcu名称
     */
    private String mcuName;

    /**
     * vdc备用使用
     */
    private String vdcMarkCascadeParticipant;

    private String casChannelName;

    /**
     * 是否welink
     */
    private Boolean isWelink;


    /**
     * 备份会场信息
     */
    private String backupMiddleUri;

    private String backupName;

    private String backupTerminalType;
}
package com.suntek.vdm.gw.smc.pojo;

import com.suntek.vdm.gw.common.pojo.BackupParticipant;
import lombok.Data;

@Data
public class ParticipantRsp {
    /**
     * 会场索引(36字符)
     */
    private String id;

    /**
     * 会场号码
     */
    private String uri;

    /**
     * 与会者名称
     */
    private String name;

    /**
     * 与会者邮箱
     */
    private String email;

    /**
     * 终端类型
     */
    private String terminalType;

    /**
     * 通信协议
     */
    private int ipProtocolType;

    /**
     * 拨入拨出模式
     */
    private String dialMode;

    /**
     * 加解密方式
     */
    private String encodeType;

    /**
     * 是否纯转发
     */
    private Boolean forward;

    /**
     * 组织名称
     */
    private String organizationName;

    /**
     * 是否是纯语音
     */
    private Boolean voice;

    /**
     * 会场速率
     */
    private Number rate;

    /**
     * 音频协议
     */
    private int audioProtocol;

    /**
     * 视频协议
     */
    private int videoProtocol;

    /**
     * 分辨率
     */
    private int videoResolution;

    /**
     * 共享能力
     */
    private int dataConfProtocol;

    /**
     * 会场所属的服务区Id
     */
    private String serviceZoneId;

    /**
     * 会场所属的服务区名称
     */
    private String serviceZoneName;

    /**
     * MCUId
     */
    private String mcuId;

    /**
     * MCU名称
     */
    private String mcuName;

    /**
     * 是否主会场
     */
    private Boolean mainParticipant;

    /**
     * 二次拨号
     */
    private String dtmfInfo;

    /**
     * 备份会场
     */
    private BackupParticipant backupParticipant;

    /**
     * TP会场
     */
    private TpParam tpParam;

    /**
     * VDC备用长度64字符
     */
    private String vdcMarkCascadeParticipant;

    /**
     * 自定义主叫号码
     */
    private String customCallingNum;
}
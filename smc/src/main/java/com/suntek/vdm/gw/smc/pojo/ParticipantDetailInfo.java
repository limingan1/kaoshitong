package com.suntek.vdm.gw.smc.pojo;

import com.suntek.vdm.gw.common.pojo.BackupParticipant;
import lombok.Data;

@Data
public class ParticipantDetailInfo {
    /**
     * 会场名称
     */
    private String name;

    /**
     * 会场标识
     */
    private String uri;

    /**
     * MCU名称
     */
    private String mcuName;

    /**
     * 速率
     */
    private int rate;

    /**
     * 通信协议
     */
    private int ipProtocolType;

    /**
     * 音频协议
     */
    private int audioProtocol;

    /**
     * 视频协议
     */
    private int videoProtocol;

    /**
     * 视频能力
     */
    private int videoResolution;

    /**
     * 会场字幕
     */
    private String participantTextTip;

    /**
     * 二次拨号
     */
    private String dtmfInfo;

    /**
     * 备份会场
     */
    private BackupParticipant backupParticipant;

    /**
     * VDC备用
     */
    private String vdcMarkCascadeParticipant;
}
package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class ParticipantState {
    /**
     * 会场Id
     */
    private String participantId;

    /**
     * 会场在线状态
     */
    private Boolean online;

    /**
     * 是否正在呼叫
     */
    private Boolean calling;

    /**
     * 是否数据入会
     */
    private Boolean dataOnline;

    /**
     * 是否音频状态
     */
    private Boolean voice;

    /**
     * 会场静音状态
     */
    private Boolean mute;

    /**
     * 会场闭音状态
     */
    private Boolean quiet;

    /**
     * 会场视频闭音状态
     */
    private Boolean videoMute;

    /**
     * 是否常用会场
     */
    private Boolean important;

    /**
     * 视频切换类型
     */
    private Integer videoSwitchAttribute;

    /**
     * 音量
     */
    private Integer volume;

    /**
     * 多画面信息
     */
    private MultiPicInfo multiPicInfo;

    /**
     * 呼叫失败原因
     */
    private Integer callFailReason;

    /**
     * 申请发言
     */
    private Boolean handUp;
}
package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class ConferenceCapabilityRsp {
    /**
     * 会议速率
     */
    private Integer rate;

    /**
     * 媒体加密模式
     */
    private Integer mediaEncrypt;

    /**
     * 音频协议
     */
    private Integer audioProtocol;

    /**
     * 视频协议
     */
    private Integer videoProtocol;

    /**
     * 视频分辨率
     */
    private Integer videoResolution;

    /**
     * 启用数据
     */
    private Boolean enableDataConf;

    /**
     * 高清实时数据会议
     */
    private Boolean enableHdRealTime;

    /**
     * 桌面共享类型
     */
    private Integer dataConfProtocol;

    /**
     * 会议类型
     */
    private String type;

    /**
     * 预留资源（0~3000）
     */
    private Integer reserveResource;

    /**
     * 录播
     */
    private Boolean enableRecord;

    /**
     * 直播
     */
    private Boolean enableLiveBroadcast;

    /**
     * 自动录播或直播
     */
    private Boolean autoRecord;

    /**
     * 是否纯语音录制
     */
    private Boolean audioRecord;

    /**
     * 是否录制桌面
     */
    private Boolean amcRecord;

    /**
     * 是否支持签到
     */
    private Boolean enableCheckIn;

    /**
     * 提前签到时间，单位分钟（0~120）
     */
    private Integer checkInDuration;

}
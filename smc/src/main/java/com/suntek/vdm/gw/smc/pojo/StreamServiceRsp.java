package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class StreamServiceRsp {
    /**
     * 是否支持直播
     */
    private Boolean supportLive;

    /**
     * 是否支持录播
     */
    private Boolean supportRecord;

    /**
     * 是否支持自动录播
     */
    private Boolean autoRecord;

    /**
     * 是否支持语音录播
     */
    private Boolean audioRecord;

    /**
     * 是否录制桌面
     */
    private Boolean amcRecord;

    /**
     * 直播地址
     */
    private String liveAddress;

    /**
     * 是否支持直播推流
     */
    private Boolean supportPushStream;

    /**
     * 是否开启自动推流
     */
    private Boolean autoPushStream;

    /**
     * 直播推流观看地址
     */
    private String playPushStreamAddress;

    /**
     * 直播推流地址
     */
    private String livePushStreamAddress;

    /**
     * 辅流推流地址
     */
    private String auxPushStreamAddress;

    /**
     * 是否支持会议纪要
     */
    private Boolean supportMinutes;
}
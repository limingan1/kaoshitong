package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class ConfConfigInfo {
    /**
     * 是否需要发送会议邮件通 知。
     * ● True：需要。
     * ● False：不需要。
     * 默认值由会议模板决定。
     */
    private Boolean isSendNotify;

    /**
     * 是否需要发送会议通知。
     * ● True：需要。
     * ● False：不需要。
     * 默认值由会议模板决定。
     */
    private Boolean isSendSms;

    /**
     * 是否需要发送会议通知。
     * ● True：需要。
     * ● False：不需要。
     * 默认值由会议模板决定。
     */
    private Boolean isSendCalendar;

    /**
     * 是否自动静音 默认值由会议 模板决定。
     */
    private Boolean isAutoMute;
}
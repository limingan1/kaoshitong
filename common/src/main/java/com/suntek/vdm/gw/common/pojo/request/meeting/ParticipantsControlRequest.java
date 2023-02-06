package com.suntek.vdm.gw.common.pojo.request.meeting;

import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import lombok.Data;

@Data
public class ParticipantsControlRequest {
    /**
     * 会场Id(36字符)
     */
    private String id;

    /**
     * 呼叫：true/挂断：false
     */
    private Boolean isOnline;

    /**
     * 静音：true/取消：false
     */
    private Boolean isMute;

    /**
     * 关闭扬声器：true/打开：FALSE
     */
    private Boolean isQuiet;


    /**
     * 打开视频源：false/关闭TRUE
     */
    private Boolean isVideoMute;

    /**
     * 音量(0~100)
     */
    private Integer volume;

    /**
     * 视频源锁定
     */
    private Integer videoSwitchAttribute;

    /**
     * 设置会场视频源
     */
    private MultiPicInfo multiPicInfo;
}


package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class DataMediaCapability {
    /**
     * 视频分辨率
     */
    private Integer protocol;

    /**
     * 数据会议协议
     */
    private Integer mediaBandWidth;
}
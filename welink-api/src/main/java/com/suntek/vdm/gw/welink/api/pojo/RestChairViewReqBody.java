package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RestChairViewReqBody {
    /**
     * 主持人观看的画面类 型。
     * ● 0： 主持人轮询
     * ● 1： 主持人观看多画
     * 面
     * ● 2： 主持人选看会场
     */
    private Integer viewType;

    /**
     * 主持人观看的画面类 型。
     * ● 0： 主持人轮询
     * ● 1： 主持人观看多画
     * 面
     * ● 2： 主持人选看会场
     */
    private String participantID;

    /**
     * 主持人轮询时，必填字 段。
     * 表示轮询间隔，单位： 秒。
     * 范围:[10-120]，默认 值：30
     */
    private Integer switchTime;

    /**
     * 主持人轮询时，必填字 段。
     * 表示轮询间隔，单位： 秒。
     * 范围:[10-120]，默认 值：30
     */
    private Integer status;
}
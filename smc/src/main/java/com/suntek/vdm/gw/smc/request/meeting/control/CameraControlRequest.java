package com.suntek.vdm.gw.smc.request.meeting.control;

import lombok.Data;

@Data
public class CameraControlRequest {
    /**
     *摄像机操作类型
     */
    private Integer operate;

    /**
     *对具体某个摄像机控制
     */
    private Integer controlType;

    /**
     *摄像机序列号
     */
    private Integer number;
}

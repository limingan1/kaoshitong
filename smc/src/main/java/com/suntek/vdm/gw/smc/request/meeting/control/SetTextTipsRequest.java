package com.suntek.vdm.gw.smc.request.meeting.control;

import lombok.Data;

@Data
public class SetTextTipsRequest {
    /**
     * 内容
     */
    private String content;

    /**
     * 操作类型
     */
    private String opType;

    /**
     * 字幕，短消息
     */
    private Integer type;

    /**
     * 位置
     */
    private Integer disPosition;

    /**
     * 效果
     */
    private Integer displayType;
}

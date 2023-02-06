package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class TxtParam {
    /**
     * 内容(中部字幕1 ~ 512字符,其他1 ~ 64字符)
     */
    private String content;

    /**
     * 操作类型
     */
    private String opType;

    /**
     * 类型
     */
    private Integer type;

    /**
     * 位置
     */
    private Integer disPosition;

    /**
     * 字幕效果
     */
    private Integer displayType;
}
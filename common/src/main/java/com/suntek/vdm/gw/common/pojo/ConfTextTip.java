package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class ConfTextTip {
    /**
     * 中部字幕
     */
    private TxtParam midCaption;

    /**
     * 横幅
     */
    private TxtParam banner;

    /**
     * 底部字幕
     */
    private TxtParam bottomCaption;
}
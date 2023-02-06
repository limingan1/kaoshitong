package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RestProlongDurReqBody {
    /**
     * 延长时间，单位为分 钟。
     * 默认值：15
     */
    private Integer duration;

    /**
     * ● 0：手动延长
     * ● 1：自动延长（未携 带延长时间时，默 认每次延长15分 钟）
     */
    private Integer auto;
}
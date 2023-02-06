package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RestChairTokenReqBody {
    /**
     * ● 1：申请主持人；
     * ● 0：释放主持人。
     */
    private Integer applyChair;

    /**
     * 当申请主持人时，由会 议AS决定是否需要密 码。
     */
    private String chairmanPwd;
}
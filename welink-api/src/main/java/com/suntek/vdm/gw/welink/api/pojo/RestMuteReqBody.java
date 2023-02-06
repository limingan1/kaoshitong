package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RestMuteReqBody {
    /**
     * ● 0：取消静音；
     * <p>
     * <p>
     * ● 1：静音。
     */
    private Integer isMute;

    private Integer allowUnmuteByOneself;

    public RestMuteReqBody(Integer isMute, Integer allowUnmuteByOneself) {
        this.isMute = isMute;
        this.allowUnmuteByOneself = allowUnmuteByOneself;
    }

    public RestMuteReqBody() {
    }
}
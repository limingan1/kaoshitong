package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RestAllowUnMuteReqBody {
    /**
     * 是否允许自己解除静音 （仅静音时有效），默 认为允许
     * ● 0： 不允许 ● 1： 允许
     */
    private Integer allowUnmuteBy;
}
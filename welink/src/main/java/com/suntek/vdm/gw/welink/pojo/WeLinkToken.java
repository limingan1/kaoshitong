package com.suntek.vdm.gw.welink.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeLinkToken {
    /**
     * token
     */
    private String accessToken;//添加welink节点token
    /**
     * Token的有效时长，单位：秒。
     */
    private Integer validPeriod;
    /**
     * Token的失效时间戳，单位：秒。
     */
    private Integer expireTime;
}

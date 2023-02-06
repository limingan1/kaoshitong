package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class AuthReqDTOV1 {
    /**
     * 用户账号（华为云会议账 号）。
     * 样例：
     * zhangsan@huawei
     * 业务账号请提前申请，具 体申请方法请参见开发流 程。
     * maxLength: 255
     * minLength: 1
     */
    private String account;

    /**
     * 登录客户端类型。
     * ● 0：Web客户端类型
     */
    private Integer clientType;

    /**
     * 是否生成Token。
     * ● 0：生成token，用于
     * 登录鉴权
     * ● 1：不生成token 默认值为0。
     */
    private Integer createTokenType;

    /**
     * 验证码信息，用于验证码 场景携带服务端返回的验 证码信息。
     * maxLength: 255
     * minLength: 0
     */
    private String HA2;
}
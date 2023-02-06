package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class UserDTO {
    /**
     * 用户ID。
     */
    private String id;

    /**
     * 查询用户详情时, 根据不同情况，响应不同。 ● 0： 查询成功且用户信息有变化， 响应会
     * 把新的信息都返回回去
     * ● 1 ：查询成功且用户信息没有变化，响应 只会返回用户ID
     * ● 2 ：用户不存在
     * ● 3 ：无权限查询这个用户
     */
    private Integer statusCode;

    /**
     * 用户账号。
     */
    private String account;

    /**
     * 用户名。
     */
    private String name;

    /**
     * 英文名。
     */
    private String englishName;

    /**
     * 邮箱。
     */
    private String email;

    /**
     * 用户手机。
     */
    private String phone;

    /**
     * 用户部门。
     */
    private String deptName;

    /**
     * 用户号码。
     */
    private String number;

    /**
     * 用户信息最后更新时间。
     */
    private String updateTime;

    /**
     * 是否为硬终端。
     */
    private Boolean isHardTerminal;

    /**
     * 用户vmr会议室ID。
     */
    private String vmrId;

    /**
     * 用户签名。
     */
    private String signature;

    /**
     * 职位。
     */
    private String title;

    /**
     * 描述信息。
     */
    private String description;
}
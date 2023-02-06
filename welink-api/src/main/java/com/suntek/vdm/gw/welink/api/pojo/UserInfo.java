package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class UserInfo {
    /**
     * 用户ID。
     */
    private String userId;

    /**
     * 用户UC帐号。
     */
    private String ucloginAccount;

    /**
     * 用户关联的号码，SIP格式。
     * 登录类型不一样获取到的号码也不同，如软终 端和硬终端、客户端登录获取的号码不同。 若 未关联号码，则该值为空。
     */
    private String serviceAccount;

    /**
     * 号码对应的HA1。
     */
    private String numberHA1;

    /**
     * 用户别名1。
     */
    private String alias1;

    /**
     * 企业ID。
     */
    private String companyId;

    /**
     * SP ID。
     */
    private String spId;

    /**
     * 企业域名。
     */
    private String companyDomain;

    /**
     * 本地鉴权。
     */
    private String realm;

    /**
     * 用户类型。
     * ● 0：系统管理用户；
     * ● 1：SP管理用户；
     * ● 2：企业用户；
     * ● 3：upath用户；
     * ● 4：硬终端默认用户；
     * ● 5：TE终端用户；
     * ● 6：顾客用户；
     * ● 7：公共设备用户；
     * ● 8：集群群组用户；
     * ● 9：USM用户。
     */
    private Integer userType;

    /**
     * 管理员类型。
     * ● 0：默认管理员；
     * ● 1：普通管理员；
     * ● 2：非管理员，即普通企业成员， “userType”为“2”时有效。
     */
    private Integer adminType;

    /**
     * 用户姓名。
     */
    private String name;

    /**
     * 用户英文姓名。
     */
    private String nameEn;
}
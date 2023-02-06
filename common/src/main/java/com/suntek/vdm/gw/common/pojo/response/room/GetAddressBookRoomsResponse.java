package com.suntek.vdm.gw.common.pojo.response.room;

import lombok.Data;

@Data
public class GetAddressBookRoomsResponse {
    /**
     *会议室uuid
     */
    private String entryUuid;

    private String uri;
    private String phone;
    private String userId;
    private String deptName;

    /**
     *会议室名称
     */
    private String name;

    /**
     *区域名称
     */
    private String areaName;

    /**
     *组织名称
     */
    private String orgName;

    /**
     *终端类型
     */
    private String terminalType;

    /**
     *终端号码
     */
    private String middleUri;

    /**
     *终端注册SC协议类型
     */
    private Integer ipProtocolType;

    /**
     *速率
     */
    private String rate;

    /**
     *描述
     */
    private String description;

    /**
     *邮箱
     */
    private String email;


    /**
     * 备份会场信息
     */
    private String backupMiddleUri;

    private String backupName;

    private String backupTerminalType;
}

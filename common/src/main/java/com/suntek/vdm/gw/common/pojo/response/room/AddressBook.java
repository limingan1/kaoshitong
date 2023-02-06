package com.suntek.vdm.gw.common.pojo.response.room;

import lombok.Data;

@Data
public class AddressBook {
    private String entryUuid;

    private String name;

    private String areaName;

    private String terminalType;

    private String middleUri;

    private Integer ipProtocolType;

    private String leftUri;

    private String rightUri;

    private String rate;

    private String description;

    private String email;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 会场UserId（企业通讯录必带）
     */
    private String userId;

    /**
     * 会场部门
     */
    private String department;

    /**
     * 是否weLink会场
     */
    private Boolean isWeLink;

    private Boolean successQuery;

    private Boolean sipState;

    private Boolean gkState;

    private Boolean connect;


    /**
     * 备份会场信息
     */
    private String backupMiddleUri;

    private String backupName;

    private String backupTerminalType;
}

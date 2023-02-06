package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RestRenamePartReqBody {
    /**
     * 与会者标识。
     * 已入会的必须填写该字 段。
     */
    private String participantID;

    /**
     * 与会者号码。
     */
    private String number;

    /**
     * 新名字。
     */
    private String newName;
}
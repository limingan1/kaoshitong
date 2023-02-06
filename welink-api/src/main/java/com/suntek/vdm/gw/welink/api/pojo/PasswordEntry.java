package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class PasswordEntry {
    /**
     * 会议角色。
     * ● “chair”：会议主持 人。
     * ● “general ”：普通与 会者。
     */
    private String conferenceRole;

    /**
     * 会议中角色的密码，明 文。
     */
    private String password;
}
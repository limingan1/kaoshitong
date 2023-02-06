package com.suntek.vdm.gw.smc.pojo;

import com.suntek.vdm.gw.common.pojo.ParticipantReq;
import lombok.Data;

@Data
public class AttendeeReq {
    /**
     * 与会人用户绑定的终端
     */
    private String uri;
    private String id;

    /**
     * 与会者名称(1~64字符)
     */
    private String name;

    /**
     * 与会者账号名称(1~64字符)
     */
    private String account;

    /**
     * 邮箱地址(可选)
     */
    private String email;

    /**
     * 组织名称(1~64字符)
     */
    private String organizationName;

    /**
     * 与会人电话号码(可选)
     */
    private String mobile;

    public ParticipantReq toParticipantReq() {
        ParticipantReq participantReq = new ParticipantReq();
        participantReq.setUri(uri);
        participantReq.setName(name);
        participantReq.setOrganizationName(organizationName);
        participantReq.setIpProtocolType(1);
        participantReq.setRate(1920);
        participantReq.setEncodeType("ENCODE_DECODE");
        participantReq.setDialMode("OUT");
        participantReq.setDtmfInfo("");
        participantReq.setNotDisplay(false);
        participantReq.setLockVideoSrc(false);
        return participantReq;
    }
}
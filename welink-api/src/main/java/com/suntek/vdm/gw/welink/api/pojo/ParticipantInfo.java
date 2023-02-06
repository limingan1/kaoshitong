package com.suntek.vdm.gw.welink.api.pojo;

import com.suntek.vdm.gw.common.pojo.CascadeParticipantParameter;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import com.suntek.vdm.gw.common.pojo.ParticipantStatusInfo;
import com.suntek.vdm.gw.common.pojo.SubPic;
import com.suntek.vdm.gw.welink.api.enumeration.Status;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
public class ParticipantInfo implements Cloneable{
    private String pId;
    private Map<String, ParticipantInfo> pIdToRepeatMap = new HashMap<>();
    /**
     * 与会者标识。
     */
    private String participantID;
    private String uri;

    /**
     * 与会者的名称（昵称）。
     */
    private String name;

    /**
     * 与会者的号码。
     */
    private String subscriberID;

    /**
     * 会议中的角色。
     * ● 1：会议主持人。
     * ● 0：普通与会者。
     */
    private Integer role;

    /**
     * 用户状态。目前固定返回 MEETTING。
     */
    private String state;
    private Boolean video;
    private Boolean mute;

    //共享辅流
    private Boolean share;
    private Boolean shareChange;

    /**
     * “Attendee”中的 address。
     */
    private String address;

    /**
     * 默认值由会议AS定义。 ● “normal”：语音、
     * 高清、标清与会者地址 （默认）。
     * ● “telepresence”：智 真与会者地址类型，单 屏、三屏智真均属此 类。（预留字段）
     * ● “terminal”：会议室 或硬终端。
     * ● “outside”：外部与 会人。
     * ● “mobile ”：软终端用 户手机。
     * ● “telephone ”：软终 端用户固定电话。
     */
    private String attendeeType;

    /**
     * 预订者的账号ID。
     */
    private String accountId;

    /**
     * 当“attendeeType”为 “telepresence”时，且 设备为三屏智真，则该字 段填写左屏号码。（预留 字段）
     */
    private String phone2;


    private String phone;
    private Status status = Status.ON_NONE;

    /**
     * 当“attendeeType”为 “telepresence”时，且 设备为三屏智真，则该字 段填写右屏号码。（预留 字段）
     */
    private String phone3;

    /**
     * 邮件地址。最大不超过 255个字符。
     */
    private String email;

    /**
     * 短信通知的手机号码。最 大不超过127个字符。
     */
    private String sms;
    private Integer mtNumber;

    /**
     * 部门名称。最大不超过96 个字符。
     */
    private String deptName;

    /**
     * 预订者的用户uuid。
     */
    private String userUUID;

    private Boolean lockView;

    /**
     * 是否主动邀请与会者
     */
    private boolean isAttend = false;

    private MultiPicInfo multiPicInfo = new MultiPicInfo();

    private String vdcMarkCascadeParticipant;

    public ParticipantInfo() {
    }

    public ParticipantInfo(String name, String phone) {
        this.name = name;
        this.uri = phone;
    }

    public ParticipantStatusInfo toParticipantStatusInfo(String conferenceId) {
        ParticipantStatusInfo info = new ParticipantStatusInfo();
        info.setParticipantId(uri);
        info.setUri(uri);
        info.setSiteUri(uri);
        info.setConferenceId(conferenceId);
        info.setName(name);
        CascadeParticipantParameter cascadeParticipantParameter = CascadeParticipantParameter.valueOf(vdcMarkCascadeParticipant);
        if(cascadeParticipantParameter != null && cascadeParticipantParameter.getIndex() == 0){
            info.setName("welink(1)");
        }
        info.setMute(mute);
        info.setShare(share);
        info.setQuiet(false);
        info.setSiteVideoMute(false);
        info.setOnline("0".equals(state));
        info.setMultiPicInfo(multiPicInfo);
        info.setCallFailReason(callFailReason);
        info.setVdcMarkCascadeParticipant(vdcMarkCascadeParticipant);
        info.setCascadeParticipantParameter(cascadeParticipantParameter);
        if(callFailReason == null){
            info.setCallFailReason(0);
        }

//        info.setQuiet(quit);
        return info;
    }
    public ParticipantStatusInfo toParticipantStatusInfo(String conferenceId,MultiPicInfo multiPicInfo) {
        ParticipantStatusInfo info = new ParticipantStatusInfo();
        info.setConferenceId(conferenceId);
        info.setMultiPicInfo(multiPicInfo);
        return info;
    }

    public void setMultiPicInfo(String uri) {
        multiPicInfo.setMode(1);
        multiPicInfo.setPicNum(1);
        SubPic subPic = new SubPic();
        subPic.setParticipantId(uri);
        multiPicInfo.setSubPicList(Collections.singletonList(subPic));
    }

    public void setMultiPicInfos() {
        multiPicInfo.setMode(1);
        multiPicInfo.setPicNum(2);
        SubPic subPic = new SubPic();
        subPic.setParticipantId("00000000-0000-0000-0000-000000000000");
        SubPic subPic2 = new SubPic();
        subPic2.setParticipantId("00000000-0000-0000-0000-000000000000");
        multiPicInfo.setSubPicList(Arrays.asList(subPic,subPic2));
    }

    public void setMultiPicInfo() {
        SubPic subPic = new SubPic("", "", "", 0);
        multiPicInfo.setMode(1);
        multiPicInfo.setPicNum(1);
        multiPicInfo.setSubPicList(Arrays.asList(subPic, subPic));
    }

    private Integer callFailReason;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class Attendee {
    /**
     * 与会者的用户uuid。
     */
    private String userUUID;
    private String uri;

    /**
     * 与会者帐号，兼容终端老版
     * 本。如果没有携带 userUUID，就通过 accountId查询用户信息。
     */
    private String accountId;

    /**
     * 与会者名称或昵称。长度限 制为96个字符。
     */
    private String name;

    /**
     * 会议中的角色。
     * ● 0：普通与会者。
     * ● 1：会议主持人。
     * ● 2：（预留字段，暂不对 外开放）
     * default: 0
     */
    private Integer role;

    /**
     * 电话号码（可支持SIP、TEL 号码格式）。最大不超过 127个字符。phone、email 和sms三者需至少填写一
     * 个。
     * 说明
     * 当“type”为
     * “telepresence”时，且设备 为三屏智真，则该字段填写中 屏号码。（三屏智真为预留字 段）
     */
    private String phone;

    /**
     * 取值类型同参数
     * “phone”。（预留字段）
     * maxLength: 127
     * minLength: 0
     * 说明
     * 当“type”为
     * “telepresence”时，且设备 为三屏智真，则该字段填写左 屏号码。
     */
    private String phone2;

    /**
     * 取值类型同参数
     * “phone”。（预留字段）
     * maxLength: 127
     * minLength: 0
     * 说明
     * 当“type”为
     * “telepresence”时，且设备 为三屏智真，则该字段填写右 屏号码。
     */
    private String phone3;

    /**
     * 邮件地址。最大不超过255 个字符。phone、email和 sms三者需至少填写一个。
     */
    private String email;

    /**
     * 短信通知的手机号码。最大 不超过32个字符。phone、 email和sms三者需至少填 写一个。
     */
    private String sms;

    /**
     * 用户入会时是否需要自动闭 音 。取值如下：
     * ● 0： 不需要闭音。 ● 1： 需要闭音。 default: 0
     */
    private Integer isMute;

    /**
     * 会议开始时是否自动邀请该 与会者。
     * ● 0：不自动邀请。 ● 1：自动邀请。 default: 1
     */
    private Integer isAutoInvite;

    /**
     * 默认值由会议AS定义，号 码类型枚举如下：
     * ● “normal”：语音、高 清、标清与会者地址 （默认），软终端用 户。
     * ● “telepresence”：智真 与会者地址类型，单 屏、三屏智真均属此 类。（预留字段）
     * ● “terminal”：会议室 或硬终端。
     * ● “outside”：外部与会 人。
     * ● “mobile ”：用户手机 号码。
     * ● “telephone ”：软终端 用户固定电话，暂不使 用。
     */
    private String type;

    /**
     * 终端所在会议室信息。（预 留字段）
     */
    private String address;

    /**
     * 部门ID，最大不超过64个字 符。
     */
    private String deptUUID;

    /**
     * 部门名称。最大不超过128 个字符。
     */
    private String deptName;

    /**
     * 是否级联通道
     */
    private Boolean cascadeChannel;


    public Attendee() {
    }

    public Attendee(String strMergeUri, String strName){
        this.name = strName;
        String cutCasNumUri = strMergeUri.split("%")[0];
        String[] arr = cutCasNumUri.split("\\$");
        if (arr.length > 0) {
            if("null".equals(arr[0])){
                this.phone = "";
            }else{
                this.phone = arr[0];
            }
        }
        if (arr.length > 1) {
            if("null".equals(arr[1])){
                this.sms = "";
            }else{
                this.sms = arr[1];
            }
        }
        if (arr.length > 2) {
            if("null".equals(arr[2]) || arr[2].contains("%")){
                this.email = "";
            }else{
                this.email = arr[2];
            }
        }
        if (arr.length > 3) {
            if("null".equals(arr[3])){
                this.accountId = "";
            }else{
                this.accountId = arr[3];
            }
        }
    }

    public Attendee(ParticipantInfo participantInfo) {
        this.name = participantInfo.getName();
        this.phone = participantInfo.getUri();

    }
}
package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ParticipantStatusInfo {
    private String participantId;
    private String conferenceId;
    private String id;
    private String name;
    private String casChannelName;
    private String uri;
    private String siteUri;
    private Boolean online;
    private String vdcMarkCascadeParticipant;
    private CascadeParticipantParameter cascadeParticipantParameter;
    private Boolean mute;
    private Boolean quiet;
    private Boolean share;
    private Boolean siteVideoMute;
    private Boolean videoMute;
    private String encodeType;
    private MultiPicInfo multiPicInfo;
    private Integer videoSwitchAttribute;
    private List<TpGeneralParam> subTpParams;
    private Integer callFailReason;

    public String getId(){
        if(participantId == null){
            return id;
        }
        return participantId;
    }

    public String getParticipantId(){
        return getId();
    }

    public ParticipantStatusInfo() {
    }

    public ParticipantStatusInfo(String conferenceId,String participantId, MultiPicInfo multiPicInfo) {
        this.conferenceId = conferenceId;
        this.participantId = participantId;
        this.id = participantId;
        this.multiPicInfo = multiPicInfo;
    }
}

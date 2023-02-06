package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.common.enums.CascadeParticipantType;
import com.suntek.vdm.gw.common.pojo.CascadeParticipantParameter;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import com.suntek.vdm.gw.common.pojo.ParticipantDetail;
import lombok.Data;

/**
 * @author meshel
 */
@Data
public class ParticipantInfo {
    private String participantId;
    private String conferenceId;
    private String uri;
    private String name;
    private Boolean online;
    private Boolean videoMute;
    private Boolean siteVideoMute;
    private String encodeType;
    private MultiPicInfo multiPicInfo;
    private CascadeParticipantParameter cascadeParticipantParameter;


    public String getConfCasId() {
        if (uri.contains("**")) {
            return uri.split("\\*\\*")[0];
        }
        return uri;
    }

    public boolean isCascadeParticipant() {
        return cascadeParticipantParameter != null;
    }

    public boolean isCascadeMainParticipant() {
        if (isCascadeParticipant()) {
            if(isCascadeParticipantH323()){
                return cascadeParticipantParameter.getIndex() == 1 || cascadeParticipantParameter.isMain();
            }
            return  cascadeParticipantParameter.isMain();
        }
        return false;
    }

    public boolean isCascadeParticipantH323() {
        if (isCascadeParticipant() && this.cascadeParticipantParameter.getCascadeParticipantType().equals(CascadeParticipantType.H323)) {
            return true;
        }
        return false;
    }


    public static ParticipantInfo valueOf(ParticipantDetail response, String conferenceId) {
        ParticipantInfo participantInfo = new ParticipantInfo();
        participantInfo.setParticipantId(response.getGeneralParam().getId());
        participantInfo.setConferenceId(conferenceId);
        participantInfo.setUri(response.getGeneralParam().getUri());
        participantInfo.setName(response.getGeneralParam().getName());
        participantInfo.setOnline(response.getState().getOnline());
        participantInfo.setMultiPicInfo(response.getState().getMultiPicInfo());
        participantInfo.setCascadeParticipantParameter(CascadeParticipantParameter.valueOf(response.getGeneralParam().getVdcMarkCascadeParticipant()));
        return participantInfo;
    }
}

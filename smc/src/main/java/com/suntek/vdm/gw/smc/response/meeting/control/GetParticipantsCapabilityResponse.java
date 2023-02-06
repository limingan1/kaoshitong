package com.suntek.vdm.gw.smc.response.meeting.control;

import com.suntek.vdm.gw.smc.pojo.ParticipantCapabilitySet;

import lombok.Data;

@Data
public class GetParticipantsCapabilityResponse  {
    /**
     * 会议Id
     */
    private String conferenceId;

    /**
     * 会场Id
     */
    private String participantId;

    /**
     * 本端能力
     */
    private ParticipantCapabilitySet localCapability;

    /**
     * 远端能力
     */
    private ParticipantCapabilitySet remoteCapability;

    /**
     * 公共能力
     */
    private ParticipantCapabilitySet commonCapability;
}

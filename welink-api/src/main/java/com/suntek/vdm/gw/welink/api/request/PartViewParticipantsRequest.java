package com.suntek.vdm.gw.welink.api.request;

import lombok.Data;

@Data
public class PartViewParticipantsRequest {
    private Integer viewType;
    private String participantID;
}

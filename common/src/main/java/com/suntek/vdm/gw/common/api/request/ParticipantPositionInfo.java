package com.suntek.vdm.gw.common.api.request;

import com.suntek.vdm.gw.common.pojo.GwId;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParticipantPositionInfo {
    private GwId gwId;
    private String conferenceId;
}

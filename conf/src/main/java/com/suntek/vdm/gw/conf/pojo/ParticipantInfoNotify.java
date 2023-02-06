package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.common.pojo.GwConferenceId;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ParticipantInfoNotify {
    private String conferenceId;
    private String confCasId;
    private Integer type;
    private List<ParticipantInfo> changeList;
    private Set<GwConferenceId> allChildConferenceIdSet;
}

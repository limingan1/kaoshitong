package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ParticipantStatusNotify {
    private String conferenceId;
    private String confCasId;
    private Integer type;
    private Integer size;
    private List<ParticipantStatusInfo> changeList;
    private Set<GwConferenceId> allChildConferenceIdSet;
}

package com.suntek.vdm.gw.welink.api.request;

import lombok.Data;

import java.util.List;

@Data
public class DelParticipantsRequest {
    private List<String> bulkHangUpParticipants;
}

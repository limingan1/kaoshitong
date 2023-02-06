package com.suntek.vdm.gw.welink.api.request;

import lombok.Data;

@Data
public class LockViewParticipantsRequest {

    private Integer status;

    public LockViewParticipantsRequest(Integer status) {
        this.status = status;
    }
}

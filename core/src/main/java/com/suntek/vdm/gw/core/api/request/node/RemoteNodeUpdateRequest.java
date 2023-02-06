package com.suntek.vdm.gw.core.api.request.node;

import lombok.Data;

@Data
public class RemoteNodeUpdateRequest {
    private String id;
    private String name ;
    private String areaCode ;
    private String smcVersion;
    private int  formType;

}

package com.suntek.vdm.gw.core.api.request.node;

import lombok.Data;

@Data
public class RemoteAddNodeVerifyRequest {
    private String  id;
    private int  formType;
    private String  areaCode;
    private String  name;
}

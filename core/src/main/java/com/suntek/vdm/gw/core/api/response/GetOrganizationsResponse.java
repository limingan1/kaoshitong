package com.suntek.vdm.gw.core.api.response;

import com.suntek.vdm.gw.common.pojo.CascadeOrganization;
import lombok.Data;

@Data
public class GetOrganizationsResponse {
    private  String msg;
    private  Integer code;
    private CascadeOrganization data;
}

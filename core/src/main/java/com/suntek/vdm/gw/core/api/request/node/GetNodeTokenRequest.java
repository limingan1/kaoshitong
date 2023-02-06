package com.suntek.vdm.gw.core.api.request.node;

import com.suntek.vdm.gw.common.pojo.GwId;
import lombok.Data;

@Data
public class GetNodeTokenRequest {
    private String id;
    private String areaCode;
    private String name;
    private String smcVersion;
    private GwId realLocalGwId;//真实本级gw id
}

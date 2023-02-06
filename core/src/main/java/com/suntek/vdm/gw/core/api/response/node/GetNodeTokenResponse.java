package com.suntek.vdm.gw.core.api.response.node;

import com.suntek.vdm.gw.core.api.request.node.GetNodeTokenRequest;
import lombok.Data;

@Data
public class GetNodeTokenResponse extends GetNodeTokenRequest {
    private String uuid;
    private String expire;
}

package com.suntek.vdm.gw.conf.api.response;

import com.suntek.vdm.gw.conf.api.request.ChildNodeInfos;
import com.suntek.vdm.gw.smc.response.meeting.templates.GetTemplatesResponse;
import lombok.Data;

import java.util.List;

@Data
public class GetTemplatesResponseEx extends GetTemplatesResponse {
    private  Integer type;
    private Integer cascadeNum;
    private List<ChildNodeInfos> child;
}

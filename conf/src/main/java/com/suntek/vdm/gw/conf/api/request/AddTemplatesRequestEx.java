package com.suntek.vdm.gw.conf.api.request;

import com.suntek.vdm.gw.smc.request.meeting.templates.AddTemplatesRequest;
import lombok.Data;

import java.util.List;
@Data
public class AddTemplatesRequestEx  extends AddTemplatesRequest {
    private Integer cascadeNum;
    private List<ChildNodeInfos> child;
}

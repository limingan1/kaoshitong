package com.suntek.vdm.gw.core.api.request.orguser;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class OrgUserDetailsResponse {
    private String id;
    private String nodeId;
    private String name;
    private String username;
    private String orgId;
}

package com.suntek.vdm.gw.core.api.response.vm;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class VmDetailsResponse {
    private String id;
    private String parentId;
    private String name;
    private String areaCode;
    private String username;
    private String orgId;
    private Date createTime;
    private Date updateTime;
    private Integer permissionSwitch;
    private List<VmDetailsResponse> child = new ArrayList<>();
}

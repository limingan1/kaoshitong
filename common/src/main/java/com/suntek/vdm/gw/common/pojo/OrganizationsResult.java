package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

import java.util.List;

@Data
public class OrganizationsResult {
    private List<CascadeOrganization> list;
    private CascadeOrganization cascadeOrganization;
    private Boolean isUseThis = false;
}

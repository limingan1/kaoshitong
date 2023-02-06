package com.suntek.vdm.gw.common.pojo.response.room;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class OrganizationsDto {
    private String entryUuid;
    private String ou;
    private List<String> orgNameList;
    private Boolean hasChildOrg;
    private Number seqInParent;

    public OrganizationsDto() {
    }

    public OrganizationsDto(String entryUuid, String ou, String orgName, Boolean hasChildOrg) {
        this.entryUuid = entryUuid;
        this.ou = ou;
        this.orgNameList = Collections.singletonList(orgName);
        this.hasChildOrg = hasChildOrg;
    }
}

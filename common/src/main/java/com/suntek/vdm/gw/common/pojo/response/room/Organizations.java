package com.suntek.vdm.gw.common.pojo.response.room;

import lombok.Data;

import java.util.List;

@Data
public class Organizations {
    private String deptNameCn;
    private String deptName;
    private String deptCode;

    private List<OrganizationsOld> childDepts;

    public boolean hasChild() {
        return childDepts != null && childDepts.size() > 0;
    }
}

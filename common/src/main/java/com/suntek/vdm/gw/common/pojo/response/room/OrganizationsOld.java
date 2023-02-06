package com.suntek.vdm.gw.common.pojo.response.room;

import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
public class OrganizationsOld extends Organizations{
    private List<OrganizationsDto> organizationResultBeanList;

    private String corpId;
    private String deptCode;
    private Integer deptLevel;
    private String deptName;
    private String deptNamePath;
    private Boolean isLeafNode;
    private String parentDeptCode;
    private String deptCodePath;
    private String note;
    private String inPermission;
    private String outPermission;
    private List<String> designatedOutDeptCodes;
    private List<String> designatedOutDeptCodesIncludeChild;
    private List<OrganizationsOld> childDepts;

    public boolean hasChild() {
        return childDepts != null && childDepts.size() > 0;
    }
}

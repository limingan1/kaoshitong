package com.suntek.vdm.gw.common.pojo.response.room;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrganizationsNew extends Organizations{

    private String deptCode;
    private String corpDeptCode;
    private String corpParentCode;
    private String parentCode;
    private String deptNameCn;
    private String deptNameEn;
    private List<String> managerId;
    private List<String> corpManagerId;
    private String orderNo;
    private Integer visibleRange;
    private Integer hasChildDept;
    private Integer createDeptGroup;
    private Integer groupContainSubDept;
    private String imDeptGroupId;
    private List<String> ext;

    private List<OrganizationsNew> childDept;

    public boolean hasChild() {
        return hasChildDept != null && hasChildDept == 1;
    }
}

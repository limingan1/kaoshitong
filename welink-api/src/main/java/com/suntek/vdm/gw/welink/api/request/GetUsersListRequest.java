package com.suntek.vdm.gw.welink.api.request;

import lombok.Data;

@Data
public class GetUsersListRequest {
    private String searchKey;
    private Integer offset;
    /**
     * ● 0：查询普通用户
     * ● 1：查询终端用户
     * ● 2：查询所有用户 (默认值)
     */
    private Integer searchScope;
    private Integer limit;
    private String deptCode;
    /**
     * 是否查询子部门下的用户。
     * 默认值：true
     */
    private Boolean querySubDept;
}

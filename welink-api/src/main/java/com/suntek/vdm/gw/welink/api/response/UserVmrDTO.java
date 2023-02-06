package com.suntek.vdm.gw.welink.api.response;

import lombok.Data;

@Data
public class UserVmrDTO {
    private String id;
    private String vmrId;
    private String vmrName;
    private String vmrPkgId;
    private String vmrPkgName;
    private Integer vmrPkgParties;
    private Integer vmrPkgLength;
    private Integer status;
}

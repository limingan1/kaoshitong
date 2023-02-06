package com.suntek.vdm.gw.welink.api.response;

import lombok.Data;

import java.util.List;

@Data
public class QueryUserResultDTO {
    private String id;
    private String userAccount;
    private String name;
    private String englishName;
    private String phone;
    private String country;
    private String email;
    private String sipNum;
    private List<UserVmrDTO> vmrList;
    private String deptCode;
    private String deptName;
    private String deptNamePath;
    private Integer userType;
    private Integer adminType;
    private String signature;
    private String title;
    private String desc;
    private CorpBasicInfoDTO corp;
    private UserFunctionDTO function;
    private Integer status;
    private Integer sortLevel;
    private Boolean hidePhone;

}

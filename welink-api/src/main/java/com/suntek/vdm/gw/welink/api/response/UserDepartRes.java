package com.suntek.vdm.gw.welink.api.response;

import com.suntek.vdm.gw.welink.api.pojo.UserDepartInfo;
import lombok.Data;

import java.util.List;

@Data
public class UserDepartRes {
    private Integer pageNo;
    private Integer pageSize;
    private Integer total;
    private List<UserDepartInfo> data;
}

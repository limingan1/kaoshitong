package com.suntek.vdm.gw.common.pojo.response.room;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class OrganizationsRes {
    private String code;
    private String message;
    private Integer pageNo;
    private Integer pageSize;
    private Integer hasMore; //0表示还有下一页 ；1表示到最后一页了
    private List<OrganizationsNew> data;
}

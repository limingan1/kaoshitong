package com.suntek.vdm.gw.welink.api.response;

import lombok.Data;

import java.util.List;

@Data
public class GetUsersListResponse {
    private Integer offset;
    private Integer limit;
    private Integer count;
    private List<WelinkSite> data;
}

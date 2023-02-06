package com.suntek.vdm.gw.common.pojo.request;

import lombok.Data;

import java.util.List;

@Data
public class GetSiteRegiesterStatusReq {
    private List<String> uris;
}

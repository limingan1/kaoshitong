package com.suntek.vdm.gw.common.pojo.response.room;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetSiteRegiesterStatusResp {
    private String uri;
    private Boolean successQuery;
    private Boolean sipState;
    private Boolean gkState;
    private Boolean connect;
}

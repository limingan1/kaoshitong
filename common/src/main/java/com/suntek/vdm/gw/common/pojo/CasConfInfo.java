package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

import java.util.List;

@Data
public class CasConfInfo {
    private String name;
    private String confCasId;
    private Boolean isWeLink;
    private List<CasConfInfo> childConf;
}

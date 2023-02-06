package com.suntek.vdm.gw.core.api.request;

import lombok.Data;

@Data
public class BasePageRequest {
    private int page;
    private int limit;
    private String sortBy;
    private String sortType;
}

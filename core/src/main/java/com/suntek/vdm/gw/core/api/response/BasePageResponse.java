package com.suntek.vdm.gw.core.api.response;

import lombok.Data;

import java.util.List;

@Data
public class BasePageResponse<T> {
    private Integer count ;
    private List<T> data;
}

package com.suntek.vdm.gw.core.pojo;

import lombok.Data;

import java.util.List;
@Data
public class TableDate<T> {
    private Integer code ;
    private Integer count ;
    private List<T> data;
    private String msg;
}

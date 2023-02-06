package com.suntek.vdm.gw.common.pojo.request;

import lombok.Data;

@Data
public class FindAreaTreeConfigReq {
    private String id;
    private String name;
    private ValueDto value;

    public FindAreaTreeConfigReq() {
    }

    public FindAreaTreeConfigReq(String id, String name, boolean value) {
        this.id = id;
        this.name = name;
        ValueDto valueDto = new ValueDto();
        valueDto.setAreaTree(value);
        this.value = valueDto;
    }
}

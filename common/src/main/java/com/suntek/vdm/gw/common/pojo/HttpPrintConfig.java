package com.suntek.vdm.gw.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HttpPrintConfig {
    private  boolean request;
    private  boolean response;
}

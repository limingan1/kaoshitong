package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class ErrorResponse {
    private String errorNo;
    private String code;
    private String errorDesc;
    private String errorType;
    private String errorDesc_zh;
}

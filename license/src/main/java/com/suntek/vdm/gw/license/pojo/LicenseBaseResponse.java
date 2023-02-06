package com.suntek.vdm.gw.license.pojo;

import lombok.Data;

@Data
public class LicenseBaseResponse<T> {
    /**
     * 响应码
     */
    private int code;
    /**
     * 内容
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;


    public T getDataValue() {
        if (data == null) {
            return null;
        }
        return data;
    }
}

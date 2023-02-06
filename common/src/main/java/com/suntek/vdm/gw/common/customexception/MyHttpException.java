package com.suntek.vdm.gw.common.customexception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MyHttpException extends  Exception{
    private int code;
    private String body;

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
    @Override
    public String toString(){
        return "code:"+code+",bady:"+body;
    }
}

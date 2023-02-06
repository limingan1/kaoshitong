package com.suntek.vdm.gw.core.customexception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseStateException extends Exception {
    private String message;
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}

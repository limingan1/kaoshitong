package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.pojo.LogEntity;

import javax.servlet.http.HttpServletRequest;

public interface LogService {
    void writeLog(LogEntity logEntity, String token, HttpServletRequest request);
}

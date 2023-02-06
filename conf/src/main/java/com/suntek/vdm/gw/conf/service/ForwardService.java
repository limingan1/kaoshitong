package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface ForwardService {
    ResponseEntity<String> forwardToSmc(HttpServletRequest request) throws MyHttpException;
}

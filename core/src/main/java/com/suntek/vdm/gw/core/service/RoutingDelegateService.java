package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.pojo.GwId;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;

public interface RoutingDelegateService {
    ResponseEntity<String> redirect(GwId id, HttpServletRequest request);
}

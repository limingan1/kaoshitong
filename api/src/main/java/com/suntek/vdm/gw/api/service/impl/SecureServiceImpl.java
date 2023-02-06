package com.suntek.vdm.gw.api.service.impl;

import com.suntek.vdm.gw.api.service.SecureService;
import com.suntek.vdm.gw.common.enums.SecureHeader;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

@Service
public class SecureServiceImpl implements SecureService {

    @Override
    public void setResponseHeader(HttpServletResponse response) {
        for (SecureHeader value : SecureHeader.values()) {
            response.addHeader(value.getKey(), value.getValue());
        }
    }
}

package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.util.HttpUtil;
import com.suntek.vdm.gw.common.util.UrlUtils;
import com.suntek.vdm.gw.conf.service.ForwardService;
import com.suntek.vdm.gw.smc.service.impl.SmcHttpServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Slf4j
@Service
public class ForwardServiceImpl extends BaseServiceImpl implements ForwardService {
    @Autowired
    @Qualifier("smcHttpServiceImpl")
    private HttpService smcHttpService;

    public ResponseEntity<String> forwardToSmc(HttpServletRequest request) throws MyHttpException {
        String url = request.getRequestURI();
        if (url.contains(SmcHttpServiceImpl.prefix)) {
            url = url.replace("conf-portal/", "");
        }
        String body = null;
        try {
            body = HttpUtil.getBody(request);
        } catch (IOException e) {

        }
        String params = request.getQueryString();
        if (params != null) {
            try {
                params = URLDecoder.decode(params, "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("URL decoder error:{}", e.getMessage());
            }
        }
        if (request.getParameter("casOrgId") != null) {
            params = UrlUtils.removeParamsByQueryString(params, "casOrgId");
        }
        if (params != null && params.length() > 0) {
            url = url + "?" + params;
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        String token = request.getHeader("Token");
        if (token != null) {
            token = getSmcToken(token);
            headers.set("Token", token);
        }
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        return smcHttpService.request(url, body, headers, method);
    }
}

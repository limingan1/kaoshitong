package com.suntek.vdm.gw.license.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.service.impl.HttpServiceImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Service
public class LicenseHttpServiceImpl extends HttpServiceImpl implements HttpService {
    public static String ip = "127.0.0.1:9001";
    public static boolean ssl = false;
    public static String prefix = "/license";


    @Override
    public ResponseEntity<String> request(String url, Object body, MultiValueMap<String, String> headers, HttpMethod method) throws MyHttpException {
        StringBuilder sb = new StringBuilder();
        sb.append(ssl ? "https://" : "http://");
        sb.append(ip);
        sb.append(prefix);
        sb.append(url);
        if (headers == null) {
            headers = new LinkedMultiValueMap<String, String>();
        }
        if (body == null) {
            body = new LinkedMultiValueMap<>();
        }
        return request(sb.toString(), body, headers, method, MediaType.APPLICATION_JSON_UTF8, "S", "License");
    }
}

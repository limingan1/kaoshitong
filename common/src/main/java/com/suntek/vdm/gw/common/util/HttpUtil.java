package com.suntek.vdm.gw.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.MultiValueMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
@Slf4j
public class HttpUtil {


    /**
     * 获取主体
     *
     * @param request
     * @return
     * @throws IOException
     */
    public static String getBody(HttpServletRequest request) throws IOException {
        return getBody(request.getInputStream());
    }

    public static String getBody(ServletInputStream servletInputStream) throws IOException {
        BufferedReader streamReader = new BufferedReader(new InputStreamReader(servletInputStream, "UTF-8"));
        StringBuilder responseStrBuilder = new StringBuilder();
        String inputStr;
        while ((inputStr = streamReader.readLine()) != null) {
            responseStrBuilder.append(inputStr);
        }
        return responseStrBuilder.toString();
    }


    /**
     * 获取全路径
     *
     * @param request
     * @return
     */
    public static String getUrlAndParameter(HttpServletRequest request) {
        String queryString = request.getQueryString();
        if (queryString!=null){
            try {
                queryString = URLDecoder.decode(queryString, "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("URL decoder error:{}", e.getMessage());
            }
        }
        return request.getRequestURI() + (queryString != null ? "?" + queryString : "");
    }

    /**
     * 转换头
     *
     * @param request
     * @return
     */
    public static MultiValueMap<String,String> parseRequestHeader(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        List<String> headerNames = Collections.list(request.getHeaderNames());
        for (String headerName : headerNames) {
            List<String> headerValues = Collections.list(request.getHeaders(headerName));
            for (String headerValue : headerValues) {
                headers.add(headerName, headerValue);
            }
        }
        return headers;
    }

    public static String getIpType(String ip) {
        boolean ipv4NoPort = ip.matches("^[0-9.]+$");
        if (ipv4NoPort) {
            return "ipv4NoPort";
        }
        boolean ipv4WithPort = ip.matches("^[0-9.]+:[0-9]{1,5}$");
        if (ipv4WithPort) {
            return "ipv4WithPort";
        }
        boolean ipv6NeedPort = ip.matches("^\\[[a-zA-Z0-9:%]+\\]$");
        if (ipv6NeedPort) {
            return "ipv6NeedPort";
        }
        boolean ipv6WithPort = ip.matches("^\\[[a-zA-Z0-9:%]+\\]:[0-9]{1,5}$");
        if (ipv6WithPort) {
            return "ipv6WithPort";
        }
        boolean ipv6NoBracket = ip.matches("^[a-zA-Z0-9:%]+$");
        if (ipv6NoBracket) {
            return "ipv6NoBracket";
        }
        boolean domainName = ip.matches("^[a-zA-Z0-9.]+$");
        if (domainName) {
            return "domainName";
        }
        return "otherType";
    }
}

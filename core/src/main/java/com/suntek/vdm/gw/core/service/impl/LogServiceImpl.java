package com.suntek.vdm.gw.core.service.impl;

import com.suntek.vdm.gw.common.constant.Constants;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.LogEntity;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.core.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
@Slf4j
public class LogServiceImpl implements LogService {
    @Autowired
    @Qualifier("httpServiceImpl")
    private HttpService httpService;

    @Async("taskExecutor")
    @Override
    public void writeLog(LogEntity logEntity,String token,HttpServletRequest request) {
        String url = Constants.localAddress + ":" + Constants.port + Constants.prefix + "/log";
        try {
            MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            header.add("token", token);
            String realIP = request.getHeader("X-Real-IP");
            if (realIP == null) {
                realIP = request.getRemoteAddr();
            }
            log.info("execute node real ip : {}", realIP);
            header.add("realIp", realIP);
            String body = httpService.put(url, logEntity, header).getBody();
            log.info("write log success:{}", body);
        }catch (MyHttpException e){
            log.error("write log error: code:{},msg:{}",e.getCode(),e.getMessage());
        }
    }
}

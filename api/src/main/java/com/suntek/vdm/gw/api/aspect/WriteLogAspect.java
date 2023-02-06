package com.suntek.vdm.gw.api.aspect;

import com.suntek.vdm.gw.common.pojo.LogEntity;
import com.suntek.vdm.gw.core.annotation.WriteLog;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.LogService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

@Aspect
@Component
@Slf4j
public class WriteLogAspect {
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private LogService logService;
    @Autowired
    private HttpServletRequest request;

    @Around("@annotation(com.suntek.vdm.gw.core.annotation.WriteLog) && @annotation(writeLog)")
    public Object writeLogAspect(ProceedingJoinPoint proceedingJoinPoint, WriteLog writeLog) {
        List<Integer> successCode = Arrays.asList(200,201,204);
        Object targetReturnObj = null;
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            targetReturnObj = proceedingJoinPoint.proceed();
            if (attributes == null) {
                return targetReturnObj;
            }
            if (targetReturnObj instanceof ResponseEntity) {
                ResponseEntity<?> response = (ResponseEntity<?>) targetReturnObj;
                HttpStatus statusCode = response.getStatusCode();
                if (successCode.contains(statusCode.value())) {
                    String token = request.getHeader("token");
                    LocalToken localToken = localTokenManageService.get(token);
                    String username = localToken == null ? "admin" : localToken.getUsername();
                    String[] source = writeLog.source();
                    String[] operation = writeLog.operation();
                    String descCh = "用户\"" + username + "\"" + operation[0] + source[0] + ",结果：成功";
                    String descEn = "User \"" + username + "\" " + operation[1] + " " + source[1] + ",result:successful";
                    //插入日志
                    LogEntity logEntity = new LogEntity(descCh, descEn, username);
                    logService.writeLog(logEntity,token,attributes.getRequest());
                }
            }
        } catch (Throwable ignored) {

        }
        return targetReturnObj;
    }
}

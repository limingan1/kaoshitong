package com.suntek.vdm.gw.api.aspect;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.pojo.HttpPrintConfig;
import com.suntek.vdm.gw.common.util.HttpLogUtil;
import com.suntek.vdm.gw.common.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;


@Aspect
@Component
@Slf4j
public class WebLogAspect {

    /**
     * 以 controller 包下定义的所有请求为切入点
     */
//    @Pointcut("(execution(public * com.suntek.vdm.gw.api.controller.controller..*.*(..))||execution(public * com.suntek.vdm.gw.api.conf.controller..*.*(..))&&" +
//            "!execution(public * com.suntek.vdm.gw.api.controller.subscribe.*.*(..)))"
//    )
    @Pointcut("(execution(public * com.suntek.vdm.gw.api.controller.conf..*.*(..))||execution(public * com.suntek.vdm.gw.api.controller.core..*.*(..)))"
    )
    public void webLog() {
    }

    /**
     * 环绕
     *
     * @param proceedingJoinPoint
     * @return
     * @throws Throwable
     */
    @Around("webLog()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 开始打印请求日志
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            Object result = proceedingJoinPoint.proceed();
            return result;
        } else {
            HttpServletRequest request = attributes.getRequest();
            String url = HttpUtil.getUrlAndParameter(request);
            String body;
            try {
                MethodSignature methodSignature = (MethodSignature) proceedingJoinPoint.getSignature();
                String[] parameterNames = methodSignature.getParameterNames();
                Object[] parameters = proceedingJoinPoint.getArgs();
                StringBuilder bodySb = new StringBuilder();
                bodySb.append("[");
                for (int i = 0; i < parameterNames.length; i++) {
                    //排除authorization 不打印
                    if ("authorization".equals(parameterNames[i])) {
                        continue;
                    }
                    //参数为空
                    if (parameters[i] == null) {
                        continue;
                    }
                    String name = parameterNames[i];
                    Object value = parameters[i];
                    if ("token".equals(name)) {
                        name = "gwHttpId";
                        StringBuilder valueSB = new StringBuilder(value.toString());
                        valueSB.setCharAt(9, '*');
                        valueSB.setCharAt(10, '*');
                        valueSB.setCharAt(11, '*');
                        valueSB.setCharAt(12, '*');
                        valueSB.setCharAt(14, '*');
                        valueSB.setCharAt(15, '*');
                        valueSB.setCharAt(16, '*');
                        valueSB.setCharAt(17, '*');
                        valueSB.setCharAt(19, '*');
                        valueSB.setCharAt(20, '*');
                        valueSB.setCharAt(21, '*');
                        valueSB.setCharAt(22, '*');
                        value = valueSB.toString();
                    }

                    StringBuilder urlParameter = new StringBuilder();
                    urlParameter.append(name);
                    urlParameter.append("=");
                    urlParameter.append(value.toString());
                    if (url.contains(urlParameter.toString())) {
                        continue;
                    }
                    if (bodySb.length() > 1) {
                        bodySb.append(",");
                    }
                    bodySb.append(name);
                    bodySb.append(":");
                    bodySb.append(JSON.toJSONString(value));
                }
                bodySb.append("]");
                body = bodySb.toString();
            } catch (Exception e) {
                body = JSON.toJSONString(proceedingJoinPoint.getArgs());
            }
            String method = request.getMethod();
            HttpPrintConfig httpPrintConfig = HttpLogUtil.filter(url);
            if (httpPrintConfig.isRequest()) {
                String gwHttpCode = request.getHeader("gw-http-code");
                String from;
                if (gwHttpCode != null) {
                    from = "GW";
                } else {
                    from = "C";
                }
                String realIP = request.getHeader("X-Real-IP");
                if (realIP==null){
                    realIP=request.getRemoteAddr();
                }
                HttpLogUtil.request(url, method, body.toString(), from, realIP, "S");
            }
            Object result = proceedingJoinPoint.proceed();
            if (httpPrintConfig.isResponse()) {
                try {
                    ResponseEntity responseEntity = (ResponseEntity) result;
                    HttpLogUtil.response(responseEntity.getStatusCodeValue(), responseEntity.getBody());
                } catch (Exception e) {
                    log.error("response error:{}", result,e);
                }
            }
            return result;
        }
    }
}

package com.suntek.vdm.gw.api.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Aspect
@Component
@Slf4j
public class PageAspect {
    @Value("${openConfigPage}")
    private boolean openConfigPage;

    @Pointcut("execution(public * com.suntek.vdm.gw.api.controller.resource..*.*(..))")
    public void page() {
    }

    @Around("page()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (!openConfigPage) {
            return new ModelAndView("404/404");
        }
        return proceedingJoinPoint.proceed();
    }
}

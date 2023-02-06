package com.suntek.vdm.gw.welink.api.aspect;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.common.service.LicenseManageService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Aspect
@Component
@Slf4j
public class WelinkLicenseAspect {
    @Autowired
    private LicenseManageService licenseManageService;
    @Pointcut("execution(public * com.suntek.vdm.gw.welink.api.service.impl.WeLinkHttpServiceImpl.*(..))")
    public void welinkLicenseAspect() {
    }

    @Around("welinkLicenseAspect()")
    public Object doAround(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        if (!licenseManageService.getWelinkLicense()) {
            throw new MyHttpException(409, GwErrorCode.NO_WELINK_LICENSE_ERROR.toString());
        }
        return proceedingJoinPoint.proceed();
    }
}

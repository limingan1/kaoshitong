package com.suntek.vdm.gw.license.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.LicenseManageService;
import com.suntek.vdm.gw.license.service.LicenseCheckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LicenseCheckServiceImpl implements LicenseCheckService {

    @Autowired
    private LicenseManageService licenseManageService;
    @Override
    public boolean hasLicense() throws MyHttpException {
        return licenseManageService.getCascadeLicense();
    }
}

package com.suntek.vdm.gw.common.service.impl;

import com.suntek.vdm.gw.common.service.LicenseManageService;
import org.springframework.stereotype.Service;

@Service
public class LicenseManageServiceImpl implements LicenseManageService {
    private boolean welinkLicense;
    private boolean cascadeLicense;

    @Override
    public void saveWelinkLicense(boolean welinkLicense) {
        this.welinkLicense = welinkLicense;
    }
    @Override
    public void saveCascadeLicense(boolean cascadeLicense) {
        this.cascadeLicense = cascadeLicense;
    }

    @Override
    public boolean getWelinkLicense() {
        return welinkLicense;
//        return true;
    }
    @Override
    public boolean getCascadeLicense() {
        //有welink或者有多级互联,级联就能能用
        return cascadeLicense || welinkLicense;
//        return true;
    }
}

package com.suntek.vdm.gw.common.service;

public interface LicenseManageService {
    void saveWelinkLicense(boolean welinkLicense);

    void saveCascadeLicense(boolean cascadeLicense);

    boolean getWelinkLicense();

    boolean getCascadeLicense();
}

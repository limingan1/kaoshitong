package com.suntek.vdm.gw.license.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.license.pojo.LicenseStatus;

public interface LicenseManagerService {

    void change() throws MyHttpException;

    LicenseStatus getLicense() throws MyHttpException;

    void initLicense() throws MyHttpException;
}

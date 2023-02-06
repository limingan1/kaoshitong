package com.suntek.vdm.gw.license.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;

public interface LicenseCheckService {
    boolean hasLicense() throws MyHttpException;
}

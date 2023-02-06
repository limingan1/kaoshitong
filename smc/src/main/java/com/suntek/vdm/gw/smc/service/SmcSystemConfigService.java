package com.suntek.vdm.gw.smc.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.response.GetSystemTimeZoneResponse;

public interface SmcSystemConfigService {
     GetSystemTimeZoneResponse getSystemTimeZone(String token) throws MyHttpException;
}

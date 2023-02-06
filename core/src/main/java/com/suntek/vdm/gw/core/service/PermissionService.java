package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import org.springframework.util.MultiValueMap;

public interface PermissionService {

    boolean checkPermission(GwId targetGwId, String token, MultiValueMap<String,String> headers) throws MyHttpException;

    boolean checkPermissionUrl(String pureUrl, GwId gwId);
}

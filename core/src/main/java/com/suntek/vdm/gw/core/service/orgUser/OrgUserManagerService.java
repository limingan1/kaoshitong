package com.suntek.vdm.gw.core.service.orgUser;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;

public interface OrgUserManagerService {
    void add(String id) throws MyHttpException;

    GetTokenResponse loginRemote(String id, String authorization) throws MyHttpException;

    void update(String id) throws MyHttpException;

    void del(String id);
}

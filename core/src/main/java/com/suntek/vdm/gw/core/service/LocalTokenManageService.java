package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;

import java.util.List;
import java.util.Map;

public interface LocalTokenManageService {
    LocalToken get(String token);

    GwId getRealLocalGwIdByToken(String token);

    LocalToken getByNodeId(String nodeId);

    void delByNodeId(String nodeId);

    String getNodeId(String token);

    void add(String token, String username, String smcToken ,long expire,boolean fromClientLogin, GwId realLocalGwId);

    void del(String token);

    boolean expired(String token);

    String getSmcToken(String token);

    KeepALiveResponse keepAlive(String token) throws BaseStateException, MyHttpException;

    void setCode(String token, String code);

    boolean contains(String token);

    List<String> getExpiredToken();
    void cleanExpired();

    Map<String, LocalToken> getTokenMap();
}

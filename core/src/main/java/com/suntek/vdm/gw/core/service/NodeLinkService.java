package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.core.api.response.node.GetNodeTokenResponse;

public interface NodeLinkService {
    void clean(String id);

    Boolean openNodeWebsocketById(String id);

    void openNodeWebsocket(String id, String token, String ip, boolean ssl);

    GetNodeTokenResponse loginNode(String id, String userName, String password, String ip, boolean ssl, String areaCode)  throws MyHttpException, Exception;
}

package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.core.api.request.node.*;
import com.suntek.vdm.gw.core.api.response.node.GetNodeTokenResponse;
import com.suntek.vdm.gw.core.api.response.node.GetRemoteInfoResponse;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;

public interface NodeConfigService {
    GetNodeTokenResponse getNodeTokens(GetNodeTokenRequest request, String ip, String authorization) throws BaseStateException;

    void add(AddNodeRequest request) throws MyHttpException;

    void update(UpdateNodeRequest request) throws MyHttpException;

    void del(String id) throws MyHttpException;

    GetTokenResponse loginSmc(LoginSmcRequest request) throws MyHttpException;

    void addNodeVerify(AddNodeVerifyRequest request) throws MyHttpException;

    void remoteAddNodeVerify(RemoteAddNodeVerifyRequest request,String authorization) throws MyHttpException;

    void updateNodeVerify(UpdateNodeVerifyRequest request) throws MyHttpException;

    void remoteUpdateNodeVerify(String authorization) throws MyHttpException;

    GetRemoteInfoResponse getRemoteInfoHandler(String authorization) throws MyHttpException;

    void remoteNodeUpdateHandler(RemoteNodeUpdateRequest request);
}

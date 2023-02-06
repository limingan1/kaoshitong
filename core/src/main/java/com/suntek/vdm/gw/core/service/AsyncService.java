package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.core.api.request.node.GetNodeTokenRequest;

public interface AsyncService {
     void getNodeTokensAfter(String ip, GetNodeTokenRequest request) ;
}

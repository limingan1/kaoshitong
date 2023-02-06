package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.request.GetChannelStatusReq;
import com.suntek.vdm.gw.conf.api.request.ProxySubscribeRequest;
import com.suntek.vdm.gw.common.pojo.CasConfInfo;

public interface CascadeService {
   void proxySubScribe(ProxySubscribeRequest proxySubscribeRequest, String token) throws MyHttpException;
   void unProxySubScribe(ProxySubscribeRequest proxySubscribeRequest, String token);
   CasConfInfo casConferenceInfos(String conferenceId);
   Boolean getChannelStatus(GetChannelStatusReq getChannelStatusReq);
}

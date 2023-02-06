package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.service.impl.RemoteGwServiceImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public interface RemoteGwService extends HttpService {
    String urlSplice(String ip, boolean ssl);

    RemoteGwServiceImpl toTop() throws MyHttpException;

    RemoteGwServiceImpl toByGwId(GwId id) throws MyHttpException;

    ResponseEntity<String> request(String url, Object body, HttpMethod method) throws MyHttpException;
}

package com.suntek.vdm.gw.core.service.impl;

import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.pojo.RemoteToken;
import com.suntek.vdm.gw.core.service.RoutingDelegateService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.HttpPrintConfig;
import com.suntek.vdm.gw.common.util.HttpLogUtil;
import com.suntek.vdm.gw.common.util.HttpUtil;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class RoutingDelegateServiceImpl implements RoutingDelegateService {
    @Autowired
    private RemoteGwService remoteGwService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private VmNodeTokenManagerService vmNodeTokenManagerService;

    public ResponseEntity<String> redirect(GwId id, HttpServletRequest request) {
        try {
            String url = HttpUtil.getUrlAndParameter(request);
            String body = HttpUtil.getBody(request);
            String method = request.getMethod();
            HttpMethod httpMethod = HttpMethod.resolve(method);
            HttpPrintConfig httpPrintConfig = HttpLogUtil.filter(url);
            if (httpPrintConfig.isRequest()) {
                String gwHttpCode = request.getHeader("gw-http-code");
                String from;
                if (gwHttpCode != null) {
                    from = "GW";
                } else {
                    from = "C";
                }
                String realIP = request.getHeader("X-Real-IP");
                 if (realIP==null){
                     realIP=request.getRemoteAddr();
                 }
                HttpLogUtil.request(url, method, body, from, realIP, "S");
            }
            MultiValueMap<String,String> headers = HttpUtil.parseRequestHeader(request);
            String token = request.getHeader("Token");
            if (!permissionService.checkPermission(id, token, headers)) {
                String pureUrl = request.getRequestURI();
                if (pureUrl.matches("/conf-portal/addressbook/organizations") || pureUrl.matches("/conf-portal/addressbook/organizations/([^/ ]+)")) {
                    return new ResponseEntity<>("{\"organizationResultBeanList\":[]}", HttpStatus.OK);
                }
                HttpLogUtil.response(409, GwErrorCode.NO_PERMISSION.toString());
                //如果是本级是虚拟节点且没有开启权限
                return new ResponseEntity<>(GwErrorCode.NO_PERMISSION.toString(), HttpStatus.CONFLICT);
            }
            ResponseEntity<String> responseEntity = remoteGwService.toByGwId(id).request(url, body, headers, httpMethod);
            if (httpPrintConfig.isResponse()) {
                HttpLogUtil.response(responseEntity.getStatusCodeValue(), responseEntity.getBody());
            }
            return responseEntity;
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(),HttpStatus.valueOf(e.getCode()));
        } catch (Exception e) {
            log.error("REDIRECT ERROR:{}",e);
            return new ResponseEntity("REDIRECT ERROR", HttpStatus.INTERNAL_SERVER_ERROR);}
    }

}

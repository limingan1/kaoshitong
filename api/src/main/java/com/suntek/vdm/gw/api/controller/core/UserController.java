package com.suntek.vdm.gw.api.controller.core;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.pojo.node.NodeStatus;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.core.annotation.PassToken;
import com.suntek.vdm.gw.core.cache.CommonCache;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.UserService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.response.KeepALiveResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/conf-portal")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private NodeManageService nodeManageService;



    @PassToken
    @GetMapping("/tokens")
    public ResponseEntity<String> getTokens(@RequestHeader("Authorization") String authorization) {
        try {
            if (!CommonCache.LOAD_STATUS) {
                return new ResponseEntity<String>("system is not loaded", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            //必须先配置本级不然登录直接回500
            if (nodeDataService.getLocal() == null) {
                return new ResponseEntity<String>("local node not config", HttpStatus.INTERNAL_SERVER_ERROR);
            }

            GetTokenResponse smcGetTokenResponse = userService.getTokens(authorization, true, null);
            com.suntek.vdm.gw.core.api.response.user.GetTokenResponse getTokenResponse = new com.suntek.vdm.gw.core.api.response.user.GetTokenResponse();
            getTokenResponse.setUuid(smcGetTokenResponse.getUuid());
            getTokenResponse.setUserType(smcGetTokenResponse.getUserType());
            getTokenResponse.setExpire(smcGetTokenResponse.getExpire());
            getTokenResponse.setPasswordExpireAfter(smcGetTokenResponse.getPasswordExpireAfter());
            String gwType = httpServletRequest.getHeader("GW-TYPE");
            if (gwType != null && "SERVER".equals(gwType)) {
                String nodeId = httpServletRequest.getHeader("GW-NODE-ID");
                localTokenManageService.setCode(smcGetTokenResponse.getUuid(), nodeId);
            }

            MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
            String smcVersion = "3.0";
            if(SystemConfiguration.smcVersionIsV2()){
                smcVersion = "2.0";
            }
            headers.add("smcVersion", smcVersion);
            return new ResponseEntity<>(JSON.toJSONString(smcGetTokenResponse), headers,  HttpStatus.OK);
        } catch (BaseStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @PassToken
    @PutMapping("/tokens")
    public ResponseEntity<String> keepAlive(@RequestHeader("Token") String token,
                                            @RequestHeader(value = "RemoteStatus", required = false) String remoteStatus
                                            ) {
        try {
            KeepALiveResponse smcKeepAliveResponse = userService.keepAlive(token);
            com.suntek.vdm.gw.core.api.response.user.KeepALiveResponse keepAliveResponse = new com.suntek.vdm.gw.core.api.response.user.KeepALiveResponse();
            keepAliveResponse.setUuid(smcKeepAliveResponse.getUuid());
            keepAliveResponse.setUserType(smcKeepAliveResponse.getUserType());
            keepAliveResponse.setExpire(smcKeepAliveResponse.getExpire());
            keepAliveResponse.setPasswordExpireAfter(smcKeepAliveResponse.getPasswordExpireAfter());
            MultiValueMap<String, String> headers = null;
            if(remoteStatus != null){
                headers = new LinkedMultiValueMap<String, String>();
                String msg = nodeManageService.getLoginMsg(remoteStatus);
                if(msg != null){
                    headers.add("Msg", msg);
                }else{
                    headers.add("Msg", "Remote node not configured");
                }
            }
            return new ResponseEntity<>(JSON.toJSONString(smcKeepAliveResponse),headers, HttpStatus.OK);
        } catch (BaseStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("/tokens")
    public ResponseEntity<String> loginOut(@RequestHeader("Token") String token) {
        try {
            boolean flag = userService.delTokens(token);
            if (flag) {
                return new ResponseEntity<>("Logout success", HttpStatus.OK);
            } else {
                return null;
            }
        } catch (BaseStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


//    public void remoteLoginAfter(String token,String nodeId){
//        VdmSmcNode vdmSmcNode=vdmSmcNodeService.getOneById(nodeId);
//        if ()
//
//    }
}

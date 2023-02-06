package com.suntek.vdm.gw.api.controller.conf;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.request.AddCasChannelReq;
import com.suntek.vdm.gw.common.pojo.request.GetChannelStatusReq;
import com.suntek.vdm.gw.common.pojo.response.AddCasChannelResp;
import com.suntek.vdm.gw.conf.api.request.ProxySubscribeRequest;
import com.suntek.vdm.gw.common.pojo.response.CasConferenceInfosResponse;
import com.suntek.vdm.gw.common.pojo.CascadeChannelFreeInfo;
import com.suntek.vdm.gw.common.pojo.CascadeChannelNotifyInfo;
import com.suntek.vdm.gw.conf.service.CascadeChannelNotifyService;
import com.suntek.vdm.gw.conf.service.CascadeService;
import com.suntek.vdm.gw.conf.service.MeetingService;
import com.suntek.vdm.gw.conf.service.ParticipantInfoManagerService;
import com.suntek.vdm.gw.conf.ws.server.WsOperate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 级联内部接口 不开放使用
 */
@RestController
@RequestMapping("/conf-portal/cascade")
@Slf4j
public class CascadeController {
    @Autowired
    private CascadeService cascadeService;
    @Autowired
    ParticipantInfoManagerService participantInfoManagerService;
    @Autowired
    private CascadeChannelNotifyService cascadeChannelNotifyService;
    @Autowired
    private WsOperate wsOperate;
    @Autowired
    private MeetingService meetingService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody ProxySubscribeRequest proxySubscribeRequest,
                                            @RequestHeader("Token") String token) {

        try {
            cascadeService.proxySubScribe(proxySubscribeRequest, token);
        } catch (MyHttpException exception) {
            return new ResponseEntity<>(exception.getBody(), HttpStatus.valueOf(exception.getCode()));
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/subscribe")
    public ResponseEntity<String> unSubscribe(@RequestBody ProxySubscribeRequest proxySubscribeRequest,
                                              @RequestHeader("Token") String token) {

        cascadeService.unProxySubScribe(proxySubscribeRequest, token);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/casConferenceInfos/{conferenceId}")
    public ResponseEntity<String> casConferenceInfos(@PathVariable String conferenceId,
                                                     @RequestHeader("Token") String token) {
        CasConferenceInfosResponse response = new CasConferenceInfosResponse();
        response.setCode(0);
        response.setData(cascadeService.casConferenceInfos(conferenceId));
        return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
    }

    @PostMapping("/channel/notify")
    public ResponseEntity<String> channelNotify(@RequestBody CascadeChannelNotifyInfo request,
                                               @RequestHeader("Token") String token) {
        cascadeChannelNotifyService.notifyHandle(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/channel/free")
    public ResponseEntity<String> channelFree(@RequestBody CascadeChannelFreeInfo request,
                                                @RequestHeader("Token") String token) {
        cascadeChannelNotifyService.notifyFree(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/channel/status")
    public ResponseEntity<String> getChannelStatus(@RequestBody GetChannelStatusReq request,
                                              @RequestHeader("Token") String token) {
        Boolean status = cascadeService.getChannelStatus(request);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status", status);
        return new ResponseEntity<>(jsonObject.toJSONString(), HttpStatus.OK);
    }

    @PostMapping("/resumeLowSubscribe/{nodeId}")
    public ResponseEntity<String> notifySource(@PathVariable String nodeId,
                                               @RequestHeader("Token") String token) {
        meetingService.resumeLowSubscribe(nodeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/addCasChannel")
    public ResponseEntity<String> addCasChannel(@RequestBody AddCasChannelReq addCasChannelReq,
                                                @RequestHeader("Token") String token){
        try {
            AddCasChannelResp addCasChannelResp = meetingService.addCasChannel(addCasChannelReq, token);
            return new ResponseEntity<>(JSON.toJSONString(addCasChannelResp), HttpStatus.OK);
        } catch (MyHttpException exception) {
            return new ResponseEntity<>(exception.getBody(), HttpStatus.valueOf(exception.getCode()));
        }

    }
}

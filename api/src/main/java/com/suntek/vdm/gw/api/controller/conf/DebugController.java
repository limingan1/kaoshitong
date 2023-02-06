package com.suntek.vdm.gw.api.controller.conf;

import com.suntek.vdm.gw.core.annotation.PassToken;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.smc.service.SmcMeetingManagementService;
import com.suntek.vdm.gw.smc.service.SmcOtherService;
import com.suntek.vdm.gw.smc.ws.stomp.MyStompSession;
import com.suntek.vdm.gw.smc.ws.stomp.SmcStompClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class DebugController {
    @Autowired
    private SmcMeetingManagementService service;
    @Autowired
    private SmcOtherService smcOtherService;
    @Autowired
    private NodeManageService nodeManageService;

    @PassToken
    @PostMapping("/close/{id}")
    public void participantsControl(@PathVariable("id") String id) {
        MyStompSession myStompSession = SmcStompClient.getWebsocketMap().get(id);
        myStompSession.getStompSession().disconnect();
    }
}

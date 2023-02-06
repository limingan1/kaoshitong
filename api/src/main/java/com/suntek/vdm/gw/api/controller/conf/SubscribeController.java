package com.suntek.vdm.gw.api.controller.conf;

import com.suntek.vdm.gw.conf.service.SubscribeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class SubscribeController {
    @Autowired
    private SubscribeService subscribeService;

    /**
     * 订阅会议状态
     *
     * @param principal
     */
    @SubscribeMapping("/conferences/status")
    public void conferencesStatus(Principal principal, StompHeaderAccessor accessor) {
        subscribeService.conferencesStatus(accessor.getSubscriptionId(), principal.getName(), accessor.getSessionId());
    }


    /**
     * 订阅会控状态
     *
     * @param conferenceId
     * @param principal
     */
    @SubscribeMapping("/conferences/{conferenceId}")
    public void conferencesControllerStatus(@DestinationVariable String conferenceId, Principal principal, StompHeaderAccessor accessor) {
        String conferencesToken = accessor.getNativeHeader("token").stream().findFirst().get();
        subscribeService.conferencesControllerStatus(conferenceId, conferencesToken, accessor.getSubscriptionId(), principal.getName(), accessor.getSessionId(),true);
    }


    /**
     * 订阅会场状态
     *
     * @param conferenceId
     * @param principal
     */
    @SubscribeMapping("/conferences/{conferenceId}/participants/general")
    public void conferencesParticipantsStatus(@DestinationVariable String conferenceId, Principal principal, StompHeaderAccessor accessor) {
        String conferencesToken = accessor.getNativeHeader("token").stream().findFirst().get();
        subscribeService.conferencesParticipantsStatus(conferenceId, conferencesToken, accessor.getSubscriptionId(), principal.getName(), accessor.getSessionId(),true);
    }


    @SubscribeMapping("/meetingRoomStatus")
    public void meetingRoomStatus( Principal principal, StompHeaderAccessor accessor) {
        subscribeService.meetingRoomStatus(accessor.getSubscriptionId(), principal.getName(), accessor.getSessionId());
    }



}

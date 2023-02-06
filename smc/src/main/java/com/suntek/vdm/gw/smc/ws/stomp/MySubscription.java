package com.suntek.vdm.gw.smc.ws.stomp;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompSession;

@Data
@AllArgsConstructor
public class MySubscription {
    private StompSession.Subscription subscription;
    private StompFrameHandler handler;
}

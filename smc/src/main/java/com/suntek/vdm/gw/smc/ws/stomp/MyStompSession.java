package com.suntek.vdm.gw.smc.ws.stomp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.messaging.simp.stomp.StompSession;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyStompSession {
    private StompSession stompSession;
    private Map<String, MySubscription> subscriptionMap;
}

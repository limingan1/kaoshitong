package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.smc.ws.stomp.MyStompSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

@Data
@AllArgsConstructor
public class ReSubscribeInfo {
    MyStompSession myStompSession;
    StompSessionHandlerAdapter stompSessionHandlerAdapter;
}

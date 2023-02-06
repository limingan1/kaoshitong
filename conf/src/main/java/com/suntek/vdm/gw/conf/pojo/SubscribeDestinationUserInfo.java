package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscribeDestinationUserInfo {
    private String user;
    private String destination;
    private String backDestination;
    private String subId;
    private SubscribeUserType type;
}

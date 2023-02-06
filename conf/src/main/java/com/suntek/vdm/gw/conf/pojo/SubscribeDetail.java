package com.suntek.vdm.gw.conf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscribeDetail {
    private String subId;
    private String destination;
}

package com.suntek.vdm.gw.common.pojo.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscribeAttachInfo {
    /**
     * 订阅业务类型
     */
    private SubscribeBusinessType businessType;
    /**
     * 业务数据 自定义存储格式
     */
    private String data;
}

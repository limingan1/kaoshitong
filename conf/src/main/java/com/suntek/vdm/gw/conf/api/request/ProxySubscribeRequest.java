package com.suntek.vdm.gw.conf.api.request;

import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeAttachInfo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProxySubscribeRequest {
    /**
     * 订阅路径
     */
    private String destination;
    /**
     * 订阅回推路径
     */
    private String backDestination;
    /**
     * 源节点ID
     */
    private GwId sourceGwId;
    /**
     * 订阅附带信息（可选）
     */
    private SubscribeAttachInfo info;
}

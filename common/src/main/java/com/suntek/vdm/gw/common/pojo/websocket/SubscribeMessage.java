package com.suntek.vdm.gw.common.pojo.websocket;

import com.suntek.vdm.gw.common.pojo.GwId;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubscribeMessage {
    /**
     * 路径
     */
    private String destination;
    /**
     * 回推路径
     */
    private String backDestination;
    /**
     * 源节点ID
     */
    private GwId sourceGwId;
    /**
     * 目标节点ID
     */
    private GwId targetGwId;

    private String message;
    /**
     * 订阅附加信息 回推中不会被替换 不可变
     */
    private SubscribeAttachInfo info;

}

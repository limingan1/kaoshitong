package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.node.GwNode;

public interface NodePushService {

    void sendTopTree(GwNode tree);

    void sendLowTree(GwNode organizationNode);

    void sendLowTree(GwId gwId, GwNode organizationNode) throws MyHttpException;

    void noticeRemoteLocalNodeUpdate();
}

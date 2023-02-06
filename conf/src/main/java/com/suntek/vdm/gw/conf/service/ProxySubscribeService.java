package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.websocket.CascadeChannelMessage;
import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeAttachInfo;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;

import java.util.List;

public interface ProxySubscribeService {
    void distributionUser(SubscribeMessage subscribeMessage);

    void distributionUser(SubscribeMessage subscribeMessage, List<SubscribeUserType> userTypeBlackList);

    void subscribe(GwId sourceGwId, String destination, String backDestination, SubscribeAttachInfo info, String token) throws MyHttpException;

    void unSubscribe(GwId sourceGwId, String destination, String backDestination, SubscribeAttachInfo info, String token);

    void addWelinkCascadeChannel(CascadeChannelMessage subscribeMessage);

    void distributionRemote(GwId sourceGwId, String destination, String backDestination, String message, SubscribeAttachInfo info);
}

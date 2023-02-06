package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.pojo.NodeKeepAliveInfo;
import com.suntek.vdm.gw.core.pojo.RemoteToken;

public interface RemoteTokenManageService {
    RemoteToken get(String id);

    void add(String id, String token, String ip, boolean ssl, String areaCode, long expire);

    void del(String id);

    String getToken(String id);

    void replace(String newId, String oldId);

    boolean expired(String id);

    void keepAlive(String id) throws BaseStateException;

    void keepAliveAll();

    boolean contains(String id);

    NodeKeepAliveInfo getNodeKeepAliveInfoById(String nodeId);

    void removeNodeKeepAliveInfoById(String nodeId);

    Boolean triggerKeepAlive(NodeData nodeData);

}

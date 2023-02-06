package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.pojo.NodeKeepAliveInfo;
import com.suntek.vdm.gw.core.pojo.RemoteToken;

public interface VmNodeTokenManagerService {
    void add(String id, String uuid, String areaCode, Long valueOf, String username);

    NodeKeepAliveInfo getNodeKeepAliveInfoById(String nodeId);

    void keepAliveAll();

    void removeNodeKeepAliveInfoById(String nodeId);

    void del(String nodeId);

    RemoteToken get(String nodeId);

    boolean expired(String token);

    Boolean triggerKeepAlive(VmNodeData vmNodeData);
}

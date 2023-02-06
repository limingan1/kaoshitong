package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.node.GwNode;

public interface RoutManageService {

    void generateTrie(GwNode gwNode);

    boolean isLocal(GwId gwId);

    GwId getWayByGwId(GwId id);

    void generateRoute(GwNode gwNode);

    void cleanRoute(String id);

    boolean isLocal(GwId gwId, String token);

    boolean isLocalVm(String token);

    GwId getCompleteGwIdBy(GwId gwId);
}

package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.core.entity.VmNodeData;

import java.util.List;


public interface VmNodeDataService extends ServiceFactory<VmNodeData> {
    BaseState add(VmNodeData vmNodeData);
    BaseState update(VmNodeData request);
    VmNodeData getOneByOrgId(String id);
    List<VmNodeData> getAll();
    VmNodeData getOneByToken(String token);

    boolean hasVmNode();

    VmNodeData getRootVmNode();
}

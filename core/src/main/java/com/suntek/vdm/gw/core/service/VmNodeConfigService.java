package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.core.api.request.vm.AddVmNodeRequest;
import com.suntek.vdm.gw.core.api.request.vm.UpdateVmNodeRequest;
import com.suntek.vdm.gw.core.api.response.vm.VmDetailsResponse;
import com.suntek.vdm.gw.core.entity.VmNodeData;


public interface VmNodeConfigService {
    void add(AddVmNodeRequest request) throws MyHttpException;

    void update(UpdateVmNodeRequest request) throws MyHttpException;

    void del(String id) throws MyHttpException;

    VmNodeData getOneById(String id);

    VmDetailsResponse list();

    void addNodeVerify(AddVmNodeRequest request) throws MyHttpException;

    void updateNodeVerify(UpdateVmNodeRequest request) throws MyHttpException;
}

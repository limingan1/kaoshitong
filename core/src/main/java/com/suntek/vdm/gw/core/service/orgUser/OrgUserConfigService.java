package com.suntek.vdm.gw.core.service.orgUser;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.core.api.request.orguser.AddOrgUserRequest;
import com.suntek.vdm.gw.core.api.request.orguser.OrgUserDetailsResponse;
import com.suntek.vdm.gw.core.api.request.orguser.UpdateOrgUserRequest;
import com.suntek.vdm.gw.core.api.request.vm.AddVmNodeRequest;
import com.suntek.vdm.gw.core.api.request.vm.UpdateVmNodeRequest;
import com.suntek.vdm.gw.core.api.response.vm.VmDetailsResponse;
import com.suntek.vdm.gw.core.entity.OrgUserData;
import com.suntek.vdm.gw.core.entity.VmNodeData;

import java.util.List;


public interface OrgUserConfigService {
    void add(AddOrgUserRequest request) throws MyHttpException;

    void update(UpdateOrgUserRequest request) throws MyHttpException;

    void del(String id) throws MyHttpException;

    OrgUserDetailsResponse getOneById(String nodeId);

    List<OrgUserDetailsResponse> list(String nodeId);

    void addNodeVerify(AddOrgUserRequest request) throws MyHttpException;

    void updateNodeVerify(UpdateOrgUserRequest request) throws MyHttpException;

    boolean hasOrgUser();

    void delByNodeId(String nodeId);
}

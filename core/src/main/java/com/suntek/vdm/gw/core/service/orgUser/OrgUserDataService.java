package com.suntek.vdm.gw.core.service.orgUser;

import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.core.entity.OrgUserData;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.service.ServiceFactory;


public interface OrgUserDataService extends ServiceFactory<OrgUserData> {
    BaseState add(OrgUserData orgUserData);
    BaseState update(OrgUserData orgUserData);
}

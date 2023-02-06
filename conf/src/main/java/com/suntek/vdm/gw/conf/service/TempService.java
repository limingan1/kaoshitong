package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.conf.pojo.CallCascadeChannelInfo;
import com.suntek.vdm.gw.conf.service.impl.TempServiceImpl;

public interface TempService {
     void callCascadeChannelAdd(String conferenceId,String pId);
     void callCascadeChannelDel(String conferenceId,String pId);
     void callCascadeChannel();
}

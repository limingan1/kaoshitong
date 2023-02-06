package com.suntek.vdm.gw.conf.api.request;


import com.suntek.vdm.gw.common.pojo.CasConfInfo;
import com.suntek.vdm.gw.smc.request.meeting.control.SetTextTipsRequest;

public class SetTextTipsRequestEx extends SetTextTipsRequest {
    /**
     * 级联会议信息
     */
    private CasConfInfo casConfInfo;

}

package com.suntek.vdm.gw.common.pojo;

import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.GwId;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeetingControlMeetingInfo {
    private String id;
    private GwId gwId;
    private SmcVersionType smcVersionType;
}

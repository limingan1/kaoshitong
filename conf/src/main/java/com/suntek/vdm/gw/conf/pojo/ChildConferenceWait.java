package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.common.pojo.GwId;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChildConferenceWait {
    private GwId gwId;
    private String id;
    private String childConfCasId;
}

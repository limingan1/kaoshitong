package com.suntek.vdm.gw.smc.pojo;

import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import lombok.Data;

@Data
public class MigrateInfo {
    private  String participantId;
    private MultiPicInfo multiPicInfo;
}

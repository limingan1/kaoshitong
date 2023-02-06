package com.suntek.vdm.gw.common.pojo.request;

import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.GwId;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AddCasChannelReq {
    private String confId;
    private int cascadeNum;
    private String upAccessCode;
    private String nodeName;
    private SmcVersionType smcVersionType;
    private GwId gwId;
}

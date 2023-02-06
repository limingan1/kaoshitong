package com.suntek.vdm.gw.common.pojo.response;

import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.GwId;
import lombok.Data;

@Data
public class AddCasChannelResp {
    private SmcVersionType remoteSmcVersionType;
    private String remoteNodeName;
    private String remoteAccessCode;
    private GwId gwId;
    private Boolean isWelink = false;
    private String chairPassword;
}

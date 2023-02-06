package com.suntek.vdm.gw.core.api.response.node;

import com.suntek.vdm.gw.common.pojo.BaseState;
import lombok.Data;

@Data
public class GetRemoteInfoResponse extends BaseState {
    private String remoteId;
    private String remoteName;
    private String remoteCode;
}

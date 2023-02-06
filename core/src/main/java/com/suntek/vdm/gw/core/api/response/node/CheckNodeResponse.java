package com.suntek.vdm.gw.core.api.response.node;

import com.suntek.vdm.gw.common.pojo.BaseState;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CheckNodeResponse extends BaseState {
    private String remoteName;
    private String remoteCode;
}

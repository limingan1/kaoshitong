package com.suntek.vdm.gw.common.pojo.response;

import com.suntek.vdm.gw.common.pojo.CasConfInfo;
import com.suntek.vdm.gw.common.pojo.response.BaseResponse;
import lombok.Data;

@Data
public class CasConferenceInfosResponse extends BaseResponse {
    private CasConfInfo data;
}

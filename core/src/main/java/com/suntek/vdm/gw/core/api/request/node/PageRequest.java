package com.suntek.vdm.gw.core.api.request.node;

import com.suntek.vdm.gw.core.api.request.BasePageRequest;
import lombok.Data;

@Data
public class PageRequest extends BasePageRequest {
    private  String  name;
}
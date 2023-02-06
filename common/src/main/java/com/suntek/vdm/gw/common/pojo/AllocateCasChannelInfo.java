package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class AllocateCasChannelInfo {
    /**
     * 级联通道信息
     */
    private CascadeChannelInfo cascadeChannelInfo;
    /**
     * 是否复用复用的不用重新设置观看
     */
    private Boolean reuse;

    public boolean find() {
        return cascadeChannelInfo != null;
    }

    public AllocateCasChannelInfo() {
        this.reuse = false;
    }
}

package com.suntek.vdm.gw.smc.pojo;

import com.suntek.vdm.gw.common.pojo.VideoResolutionCap;
import lombok.Data;

import java.util.List;

@Data
public class ParticipantVideoCapability {
    /**
     * 媒体带宽
     */
    private Integer mediaBandWidth;

    /**
     * 协议类型
     */
    private Integer videoProtocol;

    /**
     * 分辨率列表
     */
    private List<VideoResolutionCap> videoResolutionCaps;

}
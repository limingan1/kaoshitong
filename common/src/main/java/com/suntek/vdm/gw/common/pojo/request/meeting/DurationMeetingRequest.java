package com.suntek.vdm.gw.common.pojo.request.meeting;

import lombok.Data;

@Data
public class DurationMeetingRequest {
    /**
     * * 延长时长(1~1400分钟)
     */
    private Integer extendTime;
}

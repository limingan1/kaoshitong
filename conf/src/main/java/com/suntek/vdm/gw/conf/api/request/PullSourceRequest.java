package com.suntek.vdm.gw.conf.api.request;

import com.suntek.vdm.gw.common.enums.MeetingControlType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PullSourceRequest {
    private String beWatchedPId;
    /**
     * 是不是自动级联通 H323
     */
    private boolean autoCascadeChannel;


    private MeetingControlType watchMeetingControlType;

}

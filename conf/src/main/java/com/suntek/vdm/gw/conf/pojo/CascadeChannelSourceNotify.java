package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.MultiPicInfo;
import lombok.Data;

@Data
public class CascadeChannelSourceNotify {
    private String conferenceId;
    private GwId gwId;
    private String accessCode;
    private String beWatchedParticipantId;
    private CascadeParticipantDirection direction;
    private Integer index;
    private MultiPicInfo multiPicInfo;


    /**
     * 如果是级联通道观看
     * @return
     */
    public boolean isCascadeChannelWatched() {
        return beWatchedParticipantId == null;
    }
}

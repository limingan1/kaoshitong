package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.common.pojo.CascadeChannelInfo;
import com.suntek.vdm.gw.conf.service.CascadeChannelManageService;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
@AllArgsConstructor
public class WatchInfo {
    private String id;
    /**
     * 如果为-1就直接使用id
     */
    private int index;

    public WatchInfo() {
    }

    public WatchInfo(String id) {
        this.id = id;
    }

    public WatchInfo(int index) {
        this.index = index;
    }

    public String getTargetParticipantId(String conferenceId, CascadeParticipantDirection direction, String childConferenceId, CascadeChannelManageService cascadeChannelManageService) {
        if (!StringUtils.isEmpty(id)) {
            return id;
        } else {
            CascadeChannelInfo cascadeChannelInfo = cascadeChannelManageService.getCascadeChannelOne(conferenceId, direction, childConferenceId, index);
            if (cascadeChannelInfo != null) {
                return cascadeChannelInfo.getParticipantId();
            }
            return null;
        }
    }
}

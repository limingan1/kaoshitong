package com.suntek.vdm.gw.common.pojo.response.meeting;


import com.suntek.vdm.gw.common.pojo.ConferenceControlParam;
import com.suntek.vdm.gw.common.pojo.ConferenceState;
import com.suntek.vdm.gw.common.pojo.ConferenceUiParam;
import lombok.Data;

@Data
public class GetMeetingDetailResponse   {
    /**
     * smc类型
     */
    private String smcVersion;
    /**
     *会议基本参数
     */
    private ConferenceUiParam conferenceUiParam;

    /**
     *会控状态
     */
    private ConferenceState conferenceState;

    /**
     *会议控制参数
     */
    private ConferenceControlParam controlParam;
}

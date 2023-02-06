package com.suntek.vdm.gw.smc.response.meeting.control;

import com.suntek.vdm.gw.common.pojo.ConferenceControlParam;
import com.suntek.vdm.gw.common.pojo.ConferenceState;
import com.suntek.vdm.gw.common.pojo.ConferenceUiParam;

import lombok.Data;

@Data
public class SetParticipantsParameterResponse  {
    /**
     *会议基本参数
     */
    private ConferenceUiParam conferenceUiParam;

    /**
     *会议状态
     */
    private ConferenceState conferenceState;

    /**
     *会议控制参数
     */
    private ConferenceControlParam controlParam;
}

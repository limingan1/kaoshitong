package com.suntek.vdm.gw.smc.response.meeting.setting;

import com.suntek.vdm.gw.smc.pojo.ConferenceCapabilityRsp;
import com.suntek.vdm.gw.smc.pojo.ConferencePolicyRsp;
import lombok.Data;

@Data
public class GetMeetingSettingsResponse {
    /**
     * 会议默认配置Id
     */
    private String id;
    /**
     * 会议默认能力
     */
    private ConferenceCapabilityRsp capabilitySettings;
    /**
     * 会议默认策略
     */
    private ConferencePolicyRsp policySettings;
}

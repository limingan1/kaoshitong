package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.request.meeting.setting.ModifyMeetingSettingsRequest;
import com.suntek.vdm.gw.smc.response.meeting.setting.GetMeetingSettingsResponse;
import com.suntek.vdm.gw.smc.response.meeting.setting.ModifyMeetingSettingsResponse;

public interface MeetingSettingService {
    GetMeetingSettingsResponse getMeetingSettings(String token) throws MyHttpException;

    ModifyMeetingSettingsResponse modifyMeetingSettings(ModifyMeetingSettingsRequest request, String token) throws MyHttpException;
}

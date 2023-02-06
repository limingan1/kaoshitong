package com.suntek.vdm.gw.smc.adaptService;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.response.meeting.setting.GetMeetingSettingsResponse;

public interface AdaptMeetingsettingService {
    String getMeetingSettings(String token) throws MyHttpException;
}

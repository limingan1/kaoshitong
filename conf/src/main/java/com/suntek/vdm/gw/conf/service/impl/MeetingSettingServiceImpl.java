package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.conf.service.MeetingSettingService;
import com.suntek.vdm.gw.smc.request.meeting.setting.ModifyMeetingSettingsRequest;
import com.suntek.vdm.gw.smc.response.meeting.setting.GetMeetingSettingsResponse;
import com.suntek.vdm.gw.smc.response.meeting.setting.ModifyMeetingSettingsResponse;
import com.suntek.vdm.gw.smc.service.SmcMeetingSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MeetingSettingServiceImpl extends BaseServiceImpl implements MeetingSettingService {
    @Autowired
    private SmcMeetingSettingService smcMeetingSettingService;

    public GetMeetingSettingsResponse getMeetingSettings(String token) throws MyHttpException{
        return  smcMeetingSettingService.getMeetingSettings(getSmcToken(token));
    }

    public ModifyMeetingSettingsResponse modifyMeetingSettings(ModifyMeetingSettingsRequest request, String token) throws MyHttpException{
        return  smcMeetingSettingService.modifyMeetingSettings(request,getSmcToken(token));
    }


}

package com.suntek.vdm.gw.smc.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingsettingService;
import com.suntek.vdm.gw.smc.request.meeting.setting.ModifyMeetingSettingsRequest;
import com.suntek.vdm.gw.smc.response.meeting.setting.GetMeetingSettingsResponse;
import com.suntek.vdm.gw.smc.response.meeting.setting.ModifyMeetingSettingsResponse;
import com.suntek.vdm.gw.smc.service.SmcMeetingSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmcMeetingSettingServiceImpl extends SmcBaseServiceImpl implements SmcMeetingSettingService {
    @Value("${useAdapt}")
    private Boolean useAdapt;

    @Autowired
    AdaptMeetingsettingService adaptMeetingsettingService;

    @Autowired
    @Qualifier("smcHttpServiceImpl")
    private HttpService httpService;

    /**
     * 查看会议设置详情
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public GetMeetingSettingsResponse getMeetingSettings(String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingsettingService.getMeetingSettings(token);
        }else {
            response = httpService.get("/conferences/settings", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetMeetingSettingsResponse.class);
    }

    @Override
    public ModifyMeetingSettingsResponse modifyMeetingSettings(ModifyMeetingSettingsRequest request, String token) throws MyHttpException {
        String response = httpService.patch("/conferences/settings", request, tokenHandle(token)).getBody();
        return JSON.parseObject(response, ModifyMeetingSettingsResponse.class);
    }

}

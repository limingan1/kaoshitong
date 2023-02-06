package com.suntek.vdm.gw.smc.adaptService.Impl;

import com.alibaba.fastjson.JSONObject;
import com.huawei.vdmserver.common.dto.ConferenceCapability;
import com.huawei.vdmserver.common.dto.ConferencePolicy;
import com.huawei.vdmserver.common.dto.responseDto.rsp.QuerySettingsRsp;
import com.huawei.vdmserver.common.enums.*;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingsettingService;
import org.springframework.stereotype.Service;

@Service
public class AdaptMeetingsettingServiceImpl implements AdaptMeetingsettingService {
    @Override
    public String getMeetingSettings(String token) throws MyHttpException {
        JSONObject querySettingsRsp = new JSONObject();
        querySettingsRsp.put("id","34575661-e361-4f20-9ca6-f69458b5c591");
        JSONObject conferenceCapability = new JSONObject();
        conferenceCapability.put("rate", 1920);
        conferenceCapability.put("mediaEncrypt", 1);
        conferenceCapability.put("audioProtocol", 1);
        conferenceCapability.put("videoProtocol", 4);
        conferenceCapability.put("videoResolution", 16);
        conferenceCapability.put("dataConfProtocol", 0);
        conferenceCapability.put("reserveResource", 0);
        conferenceCapability.put("enableDataConf", false);
        conferenceCapability.put("type", "AVC");
        conferenceCapability.put("enableHdRealTime", false);
        conferenceCapability.put("enableRecord", false);
        conferenceCapability.put("enableLiveBroadcast", false);
        conferenceCapability.put("autoRecord", false);
        querySettingsRsp.put("capabilitySettings", conferenceCapability);
        JSONObject conferencePolicy = new JSONObject();
        conferencePolicy.put("autoExtend", true);
        conferencePolicy.put("autoEnd", true);
        conferencePolicy.put("autoMute", true);
        querySettingsRsp.put("policySettings", conferencePolicy);
        return querySettingsRsp.toJSONString();
    }
}

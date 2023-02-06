package com.suntek.vdm.gw.smc.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.smc.adaptService.AdaptOtherService;
import com.suntek.vdm.gw.smc.response.GetSystemTimeZoneResponse;
import com.suntek.vdm.gw.smc.response.meeting.setting.GetMeetingSettingsResponse;
import com.suntek.vdm.gw.smc.service.SmcSystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmcSystemConfigServiceImpl extends SmcBaseServiceImpl implements SmcSystemConfigService {
    @Value("${useAdapt}")
    private Boolean useAdapt;

    @Autowired
    @Qualifier("smcHttpServiceImpl")
    private HttpService httpService;

    @Autowired
    private AdaptOtherService adaptOtherService;

    /**
     * 获取服务器时间
     *
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public GetSystemTimeZoneResponse getSystemTimeZone(String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptOtherService.getSystemTimeZone(token);
        }else {
            response = httpService.get("/systemconfig/systemtimezone", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetSystemTimeZoneResponse.class);
    }
}

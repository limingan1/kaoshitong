package com.suntek.vdm.gw.smc.adaptService.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.huawei.vdmserver.common.dto.*;
import com.huawei.vdmserver.common.vo.ConfVO;
import com.huawei.vdmserver.smc.core.service.SmcQueryOrganizationService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.adaptService.AdaptOtherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdaptOtherServiceImpl implements AdaptOtherService {
    @Autowired
    @Qualifier("SmcQueryOrganizationService2.0")
    SmcQueryOrganizationService smcQueryOrganizationService;

    @Override
    public String getMeetingTickets(String username, String token) throws MyHttpException {
        return token;
    }

    @Override
    public String getLicense(LicenseRequestDto licenseRequestDto, String token) throws MyHttpException {
        List<LicenseResquest> reqLicLists = licenseRequestDto.getReqLicLists();
        List<LicensResponse> list = new ArrayList<>();
        LicensResponse licensResponse = new LicensResponse();
        for (LicenseResquest resquest : reqLicLists) {
            Integer resId = resquest.getResId();
            Integer resLicNum = resquest.getReqLicNum();
            licensResponse.setResId(resId);
            licensResponse.setAllowLicNum(resLicNum);
            list.add(licensResponse);
        }
        LicenseResponseDto licenseResponseDto = new LicenseResponseDto();
        licenseResponseDto.setUserName(licenseRequestDto.getUserName());
        licenseResponseDto.setRspLists(list);
        return JSON.toJSONString(licenseResponseDto);
    }

    @Override
    public String findareatreeconfig(String configName, String token) {
        Map<String, Object> map = new HashMap();
        map.put("id", "8ab9e3c469ce04e50169ce058e5d1234");
        map.put("name", configName);
        Map<String, Boolean> map2 = new HashMap();
        map2.put("areaTree", true);
        map.put("value", map2);
        return JSON.toJSONString(map);
    }

    @Override
    public String areas(String token) {
        Map<String, Object> map = new HashMap();
        map.put("areaId", "8ab9e3c469ce04e50169ce058e5d1234");
        map.put("areaName", "root");
        map.put("areaRelation", "root");
        map.put("areaIdRelation", "8ab9e3c469ce04e50169ce058e5d1234");
        JSONObject responseJson = (JSONObject) JSONObject.toJSON(map);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(responseJson);
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    @Override
    public String getSystemTimeZone(String token) {
        return null;
    }

    @Override
    public String getOrganizations(String smcToken) {
        Object object = smcQueryOrganizationService.queryOrganization(smcToken);
        return JSONObject.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
    }

    @Override
    public String getUserOrgId(String smcToken) {
        ConfVO confVO = new ConfVO();
        confVO.setToken(smcToken);
        ResponseEntityEx<?> responseEntityEx = smcQueryOrganizationService.getUserOrgId(confVO);
        Object object = responseEntityEx.getBody();
        return JSONObject.toJSONString(object, SerializerFeature.DisableCircularReferenceDetect);
    }
}

package com.suntek.vdm.gw.smc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.huawei.vdmserver.common.dto.LicenseRequestDto;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
;import com.suntek.vdm.gw.common.pojo.request.FindAreaTreeConfigReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.Organization;
import com.suntek.vdm.gw.common.pojo.response.room.AreasResp;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.smc.adaptService.AdaptOtherService;
import com.suntek.vdm.gw.smc.service.SmcOtherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SmcOtherServiceImpl  extends SmcBaseServiceImpl implements SmcOtherService {
    @Value("${useAdapt}")
    private Boolean useAdapt;

    @Autowired
    AdaptOtherService adaptOtherService;

    @Autowired
    @Qualifier("smcHttpServiceImpl")
    private HttpService httpService;

    /**
     * 获取会议服务器 ticket
     * @param username
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public String getMeetingTickets(String username, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptOtherService.getMeetingTickets(username, token);
        }else {
            response = httpService.get("/tickets?username="+username, null, tokenHandle(token)).getBody();
        }
        return response;
    }

    @Override
    public String getLicense(LicenseRequestDto licenseRequestDto, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptOtherService.getLicense(licenseRequestDto, token);
        }else {
            response = httpService.post("/conferences/license", licenseRequestDto, tokenHandle(token)).getBody();
        }
        return response;
    }

    @Override
    public FindAreaTreeConfigReq findareatreeconfig(String configName, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptOtherService.findareatreeconfig(configName, token);
        }else {
            response = httpService.get("/configs/search/findareatreeconfig?configName="+configName, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, FindAreaTreeConfigReq.class);
    }

    @Override
    public List<AreasResp> areas(String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptOtherService.areas(token);
        }else {
            response = httpService.get("/addressbook/areas", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, new TypeReference<List<AreasResp>>(){});
    }

    @Override
    public List<Organization> getOrganizations(String smcToken) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptOtherService.getOrganizations(smcToken);
        }else {
            response = httpService.get("/organizations", null, tokenHandle(smcToken)).getBody();
        }
        return JSON.parseObject(response, new TypeReference<List<Organization>>(){});
    }

    @Override
    public String getUserOrgId(String username, String smcToken) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptOtherService.getUserOrgId(smcToken);
        }else {
            response = httpService.get("/users/search/names?name=" + username, null, tokenHandle(smcToken)).getBody();
        }
        return response;
    }

}

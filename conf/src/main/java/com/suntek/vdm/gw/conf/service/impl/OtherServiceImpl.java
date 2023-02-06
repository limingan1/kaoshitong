package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.huawei.vdmserver.common.dto.LicenseRequestDto;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.request.FindAreaTreeConfigReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.Organization;
import com.suntek.vdm.gw.common.pojo.response.room.AreasResp;
import com.suntek.vdm.gw.conf.service.OtherService;
import com.suntek.vdm.gw.core.cache.CommonCache;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.smc.service.SmcOtherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OtherServiceImpl extends BaseServiceImpl implements OtherService {
    @Autowired
    private SmcOtherService smcOtherService;
    @Autowired
    private LocalTokenManageService localTokenManageService;

    @Override
    public String getMeetingTickets(String username, String token)  throws MyHttpException{
       String  tickets=smcOtherService.getMeetingTickets(username,getSmcToken(token));
       //需要对用户的获取的tickets进行管理  用于websocket鉴权的使用 置换token为smcToken进行鉴权
        Map<String,Map<String,String>> userTicketsMap=CommonCache.getUserTicketsMap();
        Map<String,String> userTicketsOneMap=userTicketsMap.get(username);
        if (userTicketsOneMap==null){
            userTicketsOneMap=new ConcurrentHashMap<>();
            userTicketsMap.put(username,userTicketsOneMap);
        }
        userTicketsOneMap.put(token,tickets);
        return  tickets;
    }

    @Override
    public String getLicense(LicenseRequestDto licenseRequestDto, String token) throws MyHttpException {
        return smcOtherService.getLicense(licenseRequestDto,getSmcToken(token));
    }

    @Override
    public FindAreaTreeConfigReq findareatreeconfig(String configName, String token) throws MyHttpException {
        return smcOtherService.findareatreeconfig(configName, getSmcToken(token));
    }

    @Override
    public List<AreasResp> areas(String token) throws MyHttpException {
        return smcOtherService.areas(getSmcToken(token));
    }

    private static List<Organization> organizationList = new ArrayList<>();
    @Override
    public List<Organization> getOrganizations(String token) throws MyHttpException {
        List<Organization> res;
        try {
            res = smcOtherService.getOrganizations(getSmcToken(CoreConfig.INTERNAL_USER_TOKEN));
            organizationList = res;
            return res;
        } catch (MyHttpException e) {
            log.error("getOrganizations warn,code:{},message:{}", e.getCode(), e.getMessage());
            return organizationList;
        }
    }

    @Override
    public String getUserOrgId(String token) {
        LocalToken localToken = localTokenManageService.get(token);
        if (localToken == null){
            return null;
        }
        String respon = null;
        try {
            respon = smcOtherService.getUserOrgId(localToken.getUsername(), localToken.getSmcToken());
        } catch (MyHttpException exception) {
            exception.printStackTrace();
        }
        JSONObject jsonObject = JSON.parseObject(respon);
        if(jsonObject == null){
            return null;
        }
        JSONObject account = jsonObject.getJSONObject("account");
        if(account == null){
            return null;
        }
        JSONObject organization = account.getJSONObject("organization");
        if(organization == null){
            return null;
        }
        return organization.getString("id");
    }
}

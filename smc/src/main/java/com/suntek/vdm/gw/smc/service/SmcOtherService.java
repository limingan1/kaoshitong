package com.suntek.vdm.gw.smc.service;

import com.huawei.vdmserver.common.dto.LicenseRequestDto;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.request.FindAreaTreeConfigReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.Organization;
import com.suntek.vdm.gw.common.pojo.response.room.AreasResp;

import java.util.List;

public interface SmcOtherService {

    /**
     * 获取会议服务器 ticket
     * @param username
     * @param token
     * @return
     * @throws MyHttpException
     */
    public String getMeetingTickets(String username, String token)  throws MyHttpException;

    String getLicense(LicenseRequestDto licenseRequestDto, String token) throws MyHttpException;

    FindAreaTreeConfigReq findareatreeconfig(String configName, String token) throws MyHttpException;

    List<AreasResp> areas(String token) throws MyHttpException;

    List<Organization> getOrganizations(String smcToken) throws MyHttpException;

    String getUserOrgId(String username, String smcToken) throws MyHttpException;
}

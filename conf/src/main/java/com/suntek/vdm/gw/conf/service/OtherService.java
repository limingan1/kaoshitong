package com.suntek.vdm.gw.conf.service;

import com.huawei.vdmserver.common.dto.LicenseRequestDto;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.request.FindAreaTreeConfigReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.Organization;
import com.suntek.vdm.gw.common.pojo.response.room.AreasResp;

import java.util.List;

public interface OtherService {
    String getMeetingTickets(String username, String token)  throws MyHttpException;

    String getLicense(LicenseRequestDto licenseRequestDto, String token)  throws MyHttpException;

    FindAreaTreeConfigReq findareatreeconfig(String configName, String token) throws MyHttpException;

    List<AreasResp> areas(String token) throws MyHttpException;

    List<Organization> getOrganizations(String token) throws MyHttpException;


    String getUserOrgId(String token);
}

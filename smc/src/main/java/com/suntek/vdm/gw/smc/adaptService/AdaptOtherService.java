package com.suntek.vdm.gw.smc.adaptService;

import com.huawei.vdmserver.common.dto.LicenseRequestDto;
import com.suntek.vdm.gw.common.customexception.MyHttpException;

public interface AdaptOtherService {
    /**
     * 获取会议服务器 ticket
     * @param username
     * @param token
     * @return
     * @throws MyHttpException
     */
    public String getMeetingTickets(String username, String token)  throws MyHttpException;

    String getLicense(LicenseRequestDto licenseRequestDto, String token) throws MyHttpException;

    String findareatreeconfig(String configName, String token);

    String areas(String token);

    String getSystemTimeZone(String token);

    String getOrganizations(String smcToken);

    String getUserOrgId(String smcToken);
}

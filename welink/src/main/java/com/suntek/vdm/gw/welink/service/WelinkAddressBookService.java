package com.suntek.vdm.gw.welink.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.welink.api.pojo.WelinkAddressToken;
import com.suntek.vdm.gw.welink.api.response.UserDepartRes;

public interface WelinkAddressBookService {

    String getTickets(String clientId,String clientSecret,String addressBookUrl,boolean checkFlag) throws MyHttpException;

    String getUserDepartCode(String userId, String accessToken,String addressBookUrl) throws MyHttpException;

    UserDepartRes getUsersDepartAllInfo(String deptCode, String pageNo, String pageSize, String accessToken,String addressBookUrl) throws MyHttpException;

    WelinkAddressToken getWelinkAddressToken();

    void removeAccessToken();
}

package com.suntek.vdm.gw.welink.api.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;
import com.suntek.vdm.gw.welink.api.pojo.WelinkAddressToken;
import com.suntek.vdm.gw.welink.api.request.GetUsersListRequest;
import com.suntek.vdm.gw.welink.api.response.GetUsersListResponse;
import com.suntek.vdm.gw.welink.api.response.UserDepartRes;

public interface WeLinkAddressBookService {
    /**
     * 通过该接口查询该企业的通讯录
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
    GetUsersListResponse getUsersList(GetUsersListRequest request,String url, String token) throws MyHttpException;

    OrganizationsOld queryOrganizations(String id, String token) throws MyHttpException;

    WelinkAddressToken getTickets(String clientId, String clientSecret,String addressBookUrl) throws MyHttpException;

    String getUserDepartCode(String userId, String token,String addressBookUrl) throws MyHttpException;

    UserDepartRes getUsersDepartAllInfo(String deptCode, String pageNo, String pageSize, String accessToken,String addressBookUrl) throws MyHttpException;

    OrganizationsOld queryOrganizationsNew(String deptCode, String token,String addressBookUrl) throws MyHttpException;

    GetUsersListResponse queryUserListNew(String url,String searchKey, String accessToken, String addressBookUrl) throws MyHttpException;
}

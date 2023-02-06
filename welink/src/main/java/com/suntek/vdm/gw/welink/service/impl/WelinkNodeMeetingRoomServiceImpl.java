package com.suntek.vdm.gw.welink.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.response.Pageable;
import com.suntek.vdm.gw.common.pojo.response.Sort;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;
import com.suntek.vdm.gw.welink.api.pojo.WelinkAddressToken;
import com.suntek.vdm.gw.welink.api.request.GetUsersListRequest;
import com.suntek.vdm.gw.welink.api.response.GetUsersListNewResponse;
import com.suntek.vdm.gw.welink.api.response.GetUsersListResponse;
import com.suntek.vdm.gw.welink.api.response.WelinkSite;
import com.suntek.vdm.gw.welink.api.service.WeLinkAddressBookService;
import com.suntek.vdm.gw.welink.service.WeLinkTokenManageService;
import com.suntek.vdm.gw.welink.service.WelinkAddressBookService;
import com.suntek.vdm.gw.welink.service.WelinkNodeMeetingRoomService;
import com.suntek.vdm.gw.welink.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WelinkNodeMeetingRoomServiceImpl implements WelinkNodeMeetingRoomService {
    @Autowired
    private WeLinkAddressBookService weLinkAddressBookService;
    @Autowired
    private WeLinkTokenManageService weLinkTokenManageService;
    @Autowired
    private WelinkAddressBookService welinkAddressBookService;

    public List<GetAddressBookRoomsResponse> getAddressBookRooms(String id, String searchType, String keyWord, String token) throws MyHttpException {
        GetUsersListRequest getUsersListRequest = new GetUsersListRequest();
//        GetUsersListResponse getUsersListResponse = weLinkAddressBookService.getUsersList(getUsersListRequest, token);
        return null;
    }

    @Override
    public GetUsersListNewResponse getAddressBookRooms(String query, String Body, String token) throws MyHttpException {
        Map<String, String[]> values = new HashMap<>();
        try {
            RequestUtil.parseParameters(values, query, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        GetUsersListRequest getUsersListRequest = new GetUsersListRequest();
        int limit = Integer.parseInt(values.get("size")[0]);
        getUsersListRequest.setLimit(limit);
        getUsersListRequest.setOffset(Integer.parseInt(values.get("page")[0]) * limit);
        getUsersListRequest.setSearchKey(values.get("keyWord")[0]);
        String deptCode = "";
        String pageNo = "1";
        String pageSize = "10";
        try {
            String[] deptCodes = values.get("id");
            if (deptCodes != null && deptCodes.length >= 1) {
                deptCode = deptCodes[0].length() >= 36 ? "0" : deptCodes[0];
                getUsersListRequest.setDeptCode(deptCodes[0].length() >= 36 ? null : deptCodes[0]);
            }
            String[] querySubDepts = values.get("querySubDept");
            if (querySubDepts != null && querySubDepts.length >= 1) {
                String querySubDept = querySubDepts[0];
                getUsersListRequest.setQuerySubDept(Boolean.valueOf(querySubDept));
            }
            String[] searchScopes = values.get("searchScope");
            if (searchScopes != null && searchScopes.length >= 1) {
                String searchScope = searchScopes[0];
                getUsersListRequest.setSearchScope(Integer.valueOf(searchScope));
            }
            String[] pageNos = values.get("page");
            if (pageNos != null && pageNos.length >= 1) {
                pageNo = pageNos[0];
            }
            String[] pageSizes = values.get("size");
            if (pageSizes != null && pageSizes.length >= 1) {
                pageSize = pageSizes[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getAddressBookRooms param error");
        }
        GetUsersListResponse getUsersListResponse;
        WelinkAddressToken accessToken = welinkAddressBookService.getWelinkAddressToken();
        if (accessToken == null) {
            String url = String.format("/usg/abs/users?offset=%s&limit=%s", getUsersListRequest.getOffset(), getUsersListRequest.getLimit());
            getUsersListResponse = weLinkAddressBookService.getUsersList(getUsersListRequest, url, weLinkTokenManageService.getToken());
        }else {
            int intSize = Integer.parseInt(pageSize);
            String url = "/contact/v2/user/list?deptCode=" + deptCode + "&pageNo=" + ("0".equals(pageNo) ? "1" : pageNo) + "&pageSize=" + Math.min(intSize, 50);
            getUsersListResponse = weLinkAddressBookService.queryUserListNew(url,values.get("keyWord")[0], accessToken.getAccessToken(), accessToken.getAddressBookUrl());
            getUsersListResponse.setLimit(Integer.parseInt(pageSize));
        }
        return transferFormatToNew(getUsersListResponse);
    }

    private GetUsersListNewResponse transferFormatToNew(GetUsersListResponse oldFormat) {
        if (oldFormat == null) {
            return null;
        }
        GetUsersListNewResponse newFormat = new GetUsersListNewResponse();
        //转换分页信息格式
        transferPageInfo(oldFormat, newFormat);
        //转换数据格式
        transferData(oldFormat, newFormat);
        return newFormat;
    }

    private void transferPageInfo(GetUsersListResponse oldFormat, GetUsersListNewResponse newFormat) {
        Sort sort = new Sort();
        Integer limit = oldFormat.getLimit() == null ? 0 : oldFormat.getLimit();
        Integer count = oldFormat.getCount() == null ? 0 : oldFormat.getCount();
        newFormat.setPageable(new Pageable(sort,oldFormat.getOffset(), limit,null,null,null));
        newFormat.setSort(sort);
        newFormat.setTotalElements(count);
        int totalPage = limit == 0 ? 0 : count / limit;
        int yu = count % limit;
        if(yu > 0){
            totalPage++;
        }
        newFormat.setTotalPages(totalPage);
        newFormat.setNumberOfElements(count > limit ? limit : count);
    }

    private void transferData(GetUsersListResponse oldFormat, GetUsersListNewResponse newFormat) {
        List<WelinkSite> data = oldFormat.getData();
        if (data == null || data.isEmpty()) {
            return;
        }
        List<GetAddressBookRoomsResponse> newData = new ArrayList<>();
        for (WelinkSite welinkSite : data) {
            GetAddressBookRoomsResponse res = new GetAddressBookRoomsResponse();
            res.setEntryUuid(welinkSite.getId());//
            res.setName(welinkSite.getName());//
            res.setOrgName(welinkSite.getDeptName());
            res.setUri(welinkSite.getNumber());//
            res.setPhone(welinkSite.getPhone());
            res.setEmail(welinkSite.getEmail());
            res.setUserId(null);
            res.setDeptName(welinkSite.getDeptName());
            res.setRate("1920 Kbit/s");
            res.setIpProtocolType(1);
            res.setTerminalType("TE");
            res.setMiddleUri(siteParamExForUri(welinkSite.getNumber(), welinkSite.getPhone(), welinkSite.getEmail(), null, welinkSite.getDeptName()));
            newData.add(res);
        }
        newFormat.setContent(newData);
    }

    private static String siteParamExForUri(String number, String phone, String email, String userId, String deptName) {
        if(number==null){
            number="";
        }
        if(phone==null){
            phone="";
        }
        if(email==null){
            email="";
        }
        if(userId==null){
            userId="";
        }
        if(deptName==null){
            deptName="";
        }
        StringBuffer result = new StringBuffer();
        result.append(number);
        result.append('$');
        result.append(phone);
        result.append('$');
        result.append(email);
        result.append('$');
        result.append(userId);
        result.append('$');
        result.append(deptName);
        return result.toString();
    }

    @Override
    public OrganizationsOld queryOrganizations(String id) throws MyHttpException {
        WelinkAddressToken accessToken = welinkAddressBookService.getWelinkAddressToken();
        if (accessToken == null) {
//            String token = weLinkTokenManageService.getToken();
//            return weLinkAddressBookService.queryOrganizations(id, token);
            OrganizationsOld res = new OrganizationsOld();
            res.setOrganizationResultBeanList(new ArrayList<>());
            return res;
        }
        return weLinkAddressBookService.queryOrganizationsNew("1".equals(id) ? "0" : id, accessToken.getAccessToken(),accessToken.getAddressBookUrl());
    }
}

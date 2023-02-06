package com.suntek.vdm.gw.welink.api.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.common.pojo.response.room.*;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.welink.api.pojo.WelinkAddressToken;
import com.suntek.vdm.gw.welink.api.request.GetUsersListRequest;
import com.suntek.vdm.gw.welink.api.response.GetUsersListResponse;
import com.suntek.vdm.gw.welink.api.response.UserDepartRes;
import com.suntek.vdm.gw.welink.api.response.WelinkSite;
import com.suntek.vdm.gw.welink.api.service.WeLinkAddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WeLinkAddressBookServiceImpl extends WeLinkBaseServiceImpl implements WeLinkAddressBookService {

    @Autowired
    @Qualifier("weLinkHttpServiceImpl")
    private HttpService httpService;

//    public static String baseUrl = "https://open.welink.huaweicloud.com/api";
    public static String baseUrlPrefix = "/api";

    /**
     * 通过该接口查询该企业的通讯录
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
    public GetUsersListResponse getUsersList(GetUsersListRequest request,String url, String token) throws MyHttpException {
        if (request.getOffset()==null){
            request.setOffset(0);
        }
        if (request.getLimit()==null){
            request.setLimit(20);
        }
        if (!StringUtils.isEmpty(request.getSearchKey())){
            url+="&searchKey="+request.getSearchKey();
        }
        if (request.getSearchScope()!=null){
            url+="&searchScope="+request.getSearchScope();
        }
        if (request.getQuerySubDept() != null) {
            url += "&querySubDept=" + request.getQuerySubDept();
        }
        if (request.getDeptCode() != null) {
            url += "&deptCode=" + request.getDeptCode();
        }
        String response = httpService.get(url,null, tokenHandle(token)).getBody();
        return JSON.parseObject(response, GetUsersListResponse.class);
    }

    @Override
    public OrganizationsOld queryOrganizations(String id, String token) throws MyHttpException {
        String url = "/usg/dcs/member/dept/" + id;
        String response = httpService.get(url,null, tokenHandle(token)).getBody();
        OrganizationsOld organizationsOld = JSON.parseObject(response, OrganizationsOld.class);
        return adaptData(organizationsOld.getChildDepts());
    }

    @Override
    public WelinkAddressToken getTickets(String clientId, String clientSecret,String addressBookUrl) throws MyHttpException {
        String url = addressBookUrl + baseUrlPrefix +"/auth/v2/tickets";
        String body = "{\"client_id\":\""+clientId+"\","+" \"client_secret\":\""+clientSecret+"\"}";
        String res = httpService.post(url, body, new LinkedMultiValueMap<>()).getBody();
        JSONObject jsonObject = JSONObject.parseObject(res, JSONObject.class);
        String code = jsonObject.getString("code");
        switch (code) {
            case "0":
                break;
            case "501":
                throw new MyHttpException(409, GwErrorCode.PLEASE_CHECK_URL.toString());
            default:
                throw new MyHttpException(409, GwErrorCode.PLEASE_CHECK_CLIENT_ID.toString());
        }
        return new WelinkAddressToken(jsonObject.getString("access_token"), jsonObject.getIntValue("expires_in"), clientId, clientSecret, addressBookUrl);
    }

    @Override
    public String getUserDepartCode(String userId, String token,String addressBookUrl) throws MyHttpException {
        String url = addressBookUrl + baseUrlPrefix + "/contact/v1/users?userId=" + userId;
        String res = httpService.post(url, null, addressBookTokenHandle(token)).getBody();
        JSONObject jsonObject = JSONObject.parseObject(res, JSONObject.class);
        return jsonObject.getString("deptCode");
    }

    @Override
    public UserDepartRes getUsersDepartAllInfo(String deptCode, String pageNo, String pageSize, String accessToken,String addressBookUrl) throws MyHttpException {
        String url = addressBookUrl + baseUrlPrefix + "/contact/v1/user/users?deptCode=" + deptCode + "&pageNo=" + pageNo + "&pageSize=" + pageSize;
        String res = httpService.get(url, null, addressBookTokenHandle(accessToken)).getBody();
        return JSONObject.parseObject(res, UserDepartRes.class);
    }

    @Override
    public OrganizationsOld queryOrganizationsNew(String deptCode, String token,String addressBookUrl) throws MyHttpException {
        List<OrganizationsNew> result = new ArrayList<>();
        int pageNo = 1;
        while (true) {
            String url = addressBookUrl + baseUrlPrefix + "/contact/v2/department/list?deptCode=" + deptCode + "&recursiveflag=1&pageNo=" + pageNo + "&pageSize=30";
            String response = httpService.get(url, null, addressBookTokenHandle(token)).getBody();
            OrganizationsRes organizationsResp = JSON.parseObject(response, OrganizationsRes.class);
            if (new Integer(0).equals(organizationsResp.getHasMore())) { //0表示还有下一页 ；1表示到最后一页了
                pageNo++;
                result.addAll(organizationsResp.getData());
            } else {
                result.addAll(organizationsResp.getData());
                break;
            }
        }
        List<OrganizationsNew> list = build(result, deptCode);
        return adaptData(list);
    }

    public OrganizationsOld adaptData(List<? extends Organizations> data){
        OrganizationsOld result = new OrganizationsOld();
        if (data == null || data.isEmpty()) {
            return result;
        }
        List<OrganizationsDto> organizationsDtoList = new ArrayList<>();
        organizationsDtoList.add(new OrganizationsDto("7649fc39-81ba-4649-a237-5454502e9ff2", "VC", "VC", true));
        int seqInParent = 1;
        for (Organizations childDept : data) {
            String deptName = childDept instanceof OrganizationsNew ? childDept.getDeptNameCn() : childDept.getDeptName();
            OrganizationsDto organization = new OrganizationsDto();
            organization.setEntryUuid(childDept.getDeptCode());
            organization.setOu(deptName);
            organization.setHasChildOrg(childDept.hasChild());
            List<String> orgNameList = new ArrayList<>();
            orgNameList.add("VC");
            orgNameList.add(deptName);
            organization.setOrgNameList(orgNameList);
            organization.setSeqInParent(seqInParent);
            organizationsDtoList.add(organization);
            seqInParent += 2;
        }
        result.setOrganizationResultBeanList(organizationsDtoList);
        return result;
    }

    public List<OrganizationsNew> build(List<OrganizationsNew> data,String deptCode) {
        List<OrganizationsNew> collection = data.stream().filter(item -> deptCode.equals(item.getParentCode())).collect(Collectors.toList());
        if (collection.isEmpty()) {
            return collection;
        }
        for (OrganizationsNew item : collection) {
            item.setChildDept(build(data,item.getDeptCode()));
        }
        return collection;
    }

    @Override
    public GetUsersListResponse queryUserListNew(String url,String searchKey,String accessToken, String addressBookUrl) throws MyHttpException{
        String response = httpService.get(addressBookUrl + baseUrlPrefix + url, null, addressBookTokenHandle(accessToken)).getBody();
        GetUsersListResponse res = new GetUsersListResponse();
        JSONObject jsonObject = JSONObject.parseObject(response);
        if (!"0".equals(jsonObject.getString("code"))) {
            return res;
        }
        res.setOffset(0);
        JSONArray arrayData = jsonObject.getJSONArray("data");
        res.setLimit(arrayData.size());
        res.setCount(arrayData.size());
        List<WelinkSite> data = new ArrayList<>();
        for (Object obj : arrayData) {
            JSONObject item = JSONObject.parseObject(JSON.toJSONString(obj));
            WelinkSite welinkSite = new WelinkSite();
            welinkSite.setId(item.getString("userId"));
            welinkSite.setAccount(item.getString("userId"));
            welinkSite.setName(item.getString("userNameCn"));
            welinkSite.setEmail(item.getString("userEmail"));
            welinkSite.setPhone(item.getString("mobileNumber"));
            welinkSite.setNumber(item.getString("sipNum"));
            data.add(welinkSite);
        }
        res.setData(data.stream().filter(item ->
                searchKey == null || "".equals(searchKey) ||
                item.getName().contains(searchKey) ||
                item.getId().contains(searchKey)
        ).collect(Collectors.toList()));
        return res;
    }
}

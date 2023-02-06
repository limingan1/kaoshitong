package com.suntek.vdm.gw.smc.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsComnditionsResp;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsDto;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingRoomsService;
import com.suntek.vdm.gw.smc.service.SmcNodeMeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SmcNodeMeetingRoomServiceImpl extends SmcBaseServiceImpl implements SmcNodeMeetingRoomService {
    @Value("${useAdapt}")
    private Boolean useAdapt;

    @Autowired
    AdaptMeetingRoomsService adaptMeetingRoomsService;

    @Autowired
    @Qualifier("smcHttpServiceImpl")
    private HttpService httpService;

    /**
     * 从企业通讯录查询会议室信息
     * @param id
     * @param searchType
     * @param keyWord
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public List<GetAddressBookRoomsResponse> getAddressBookRooms(String id, String searchType, String keyWord, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingRoomsService.getAddressBookRooms(id, searchType, keyWord, token);
        }else {
            String url = String.format("/addressbook/rooms?id=%s&searchType=%s&keyWord=%s",id,searchType,keyWord);
            response = httpService.get(url, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response,new TypeReference<List<GetAddressBookRoomsResponse>>(){});
    }

    @Override
    public GetAddressBookRoomsComnditionsResp roomsConditions(String id, String keyWord, Integer page, Integer size, String searchType, String token) throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingRoomsService.roomsConditions(id, keyWord, page, size, searchType, token);
        }else {
            String url = String.format("/addressbook/rooms/conditions?id=%s&keyWord=%s&page=%s&size=%s&searchType=%s",id,keyWord,page,size,searchType);
            response = httpService.get(url, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(serializerHelp(response),GetAddressBookRoomsComnditionsResp.class);
    }

    //有的接口pageable属性返回的是对象，有的返回的是String，返回string的时候会导致序列化失败,需要特殊处理
    public static String serializerHelp(String res) {
        String result = res;
        Pattern pattern = Pattern.compile("\"(pageable|sort)\":[ ]{0,2}\"([^{},\"]+)\"?");
        Matcher matcher = pattern.matcher(res);
        if (matcher.find()) { //找到了pageable或者sort对象序列化前是string类型
            result = matcher.replaceAll("\"$1\": {}"); //替换成对象类型
        }
        return result;
    }

    @Override
    public OrganizationsOld queryOrganizations(String id, String token) throws MyHttpException{
        String response;
        if (useAdapt) {
            //smc2.0空实现
            return null;
        } else {
            String url = "/addressbook/organizations/" + (id != null ? id : "");
            response = httpService.get(url, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, OrganizationsOld.class);
    }
    @Override
    public OrganizationsOld queryOrganizations(List<String> ids, String token) throws MyHttpException{
        OrganizationsOld result = new OrganizationsOld();
        List<OrganizationsDto> organizationResultBeanList = new ArrayList<>();
        if (useAdapt) {
            //smc2.0空实现
            return null;
        } else {
            for (String id : ids) {
                String url = "/addressbook/organizations/" + (id != null ? id : "");
                String response = httpService.get(url, null, tokenHandle(token)).getBody();
                OrganizationsOld data = JSON.parseObject(response, OrganizationsOld.class);
                if (data != null && data.getOrganizationResultBeanList() != null && !data.getOrganizationResultBeanList().isEmpty()) {
                    organizationResultBeanList.addAll(data.getOrganizationResultBeanList());
                }
            }
        }
        result.setOrganizationResultBeanList(organizationResultBeanList);
        return result;
    }

    @Override
    public String queryAddressBookUsers(String orgEntryUuid, String keyWord, String page, String size, String middleUriFilter,String token) throws MyHttpException{
        String response;
        if (useAdapt) {
            return "{\"content\":[],\"pageable\":{\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"pageSize\":10,\"pageNumber\":0,\"offset\":0,\"paged\":true,\"unpaged\":false},\"totalPages\":1,\"totalElements\":0,\"last\":true,\"first\":true,\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"numberOfElements\":0,\"size\":10,\"number\":0,\"empty\":true}";
        } else {
            String url = "/addressbook/users/conditions?orgEntryUuid=" + orgEntryUuid + "&keyWord=" + keyWord + "&page=" + page + "&size=" + size + "&middleUriFilter=" + middleUriFilter;
            response = httpService.get(url, null, tokenHandle(token)).getBody();
        }
        return response;
    }
}

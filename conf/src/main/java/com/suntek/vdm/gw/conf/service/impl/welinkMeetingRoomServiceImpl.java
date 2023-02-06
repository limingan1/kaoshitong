package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsComnditionsResp;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;
import com.suntek.vdm.gw.conf.service.SmcMeetingRoomService;
import com.suntek.vdm.gw.welink.service.WelinkNodeMeetingRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class welinkMeetingRoomServiceImpl implements SmcMeetingRoomService {
    @Autowired
    private WelinkNodeMeetingRoomService welinkNodeMeetingRoomService;

    @Override
    public List<GetAddressBookRoomsResponse> getAddressBookRooms(String id, String searchType, String keyWord, String token) throws MyHttpException {
        return welinkNodeMeetingRoomService.getAddressBookRooms(id,searchType,keyWord,token);
    }

    @Override
    public GetAddressBookRoomsComnditionsResp roomsConditions(String id, String keyWord, Integer page, Integer size, String searchType, String smcToken) {
        return null;
    }

    @Override
    public OrganizationsOld queryOrganizations(String id, String token, GwId gwId) throws MyHttpException {

        return welinkNodeMeetingRoomService.queryOrganizations(id);
    }

    @Override
    public String queryAddressBookUsers(String orgEntryUuid, String keyWord, String page, String size, String middleUriFilter, String token) throws MyHttpException {
        return "{\"content\":[],\"pageable\":{\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"pageSize\":10,\"pageNumber\":0,\"offset\":0,\"paged\":true,\"unpaged\":false},\"totalPages\":1,\"totalElements\":0,\"last\":true,\"first\":true,\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"numberOfElements\":0,\"size\":10,\"number\":0,\"empty\":true}";
    }

}

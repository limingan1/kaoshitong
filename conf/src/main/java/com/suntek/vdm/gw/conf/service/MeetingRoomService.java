package com.suntek.vdm.gw.conf.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsComnditionsResp;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;

import java.util.List;

public interface MeetingRoomService {
    List<GetAddressBookRoomsResponse> getAddressBookRooms(String id, String searchType, String keyWord, String casOrgId, String token, GwId gwId)  throws MyHttpException;

    GetAddressBookRoomsComnditionsResp roomsConditions(String id, String keyWord, Integer page, Integer size, String casOrgId, String token, String searchType, GwId valueOf) throws MyHttpException;

    OrganizationsOld queryOrganizations(String id, GwId gwId, String token,GwId casOrgGwId) throws MyHttpException;

    String queryAddressBookUsers(String token, String orgEntryUuid, String keyWord, String page,String size, String middleUriFilter, GwId gwId) throws MyHttpException;
}

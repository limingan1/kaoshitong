package com.suntek.vdm.gw.smc.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsComnditionsResp;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;

import java.util.List;

public interface SmcNodeMeetingRoomService {
    /**
     * 从企业通讯录查询会议室信息
     * @param id
     * @param searchType
     * @param keyWord
     * @param token
     * @return
     * @throws MyHttpException
     */
    List<GetAddressBookRoomsResponse> getAddressBookRooms(String id, String searchType, String keyWord, String token) throws MyHttpException;

    GetAddressBookRoomsComnditionsResp roomsConditions(String id, String keyWord, Integer page, Integer size, String searchType, String token) throws MyHttpException;

    OrganizationsOld queryOrganizations(String id, String token) throws MyHttpException;

    OrganizationsOld queryOrganizations(List<String> ids, String token) throws MyHttpException;

    String queryAddressBookUsers(String orgEntryUuid, String keyWord, String page, String size, String middleUriFilter, String token) throws MyHttpException;
}

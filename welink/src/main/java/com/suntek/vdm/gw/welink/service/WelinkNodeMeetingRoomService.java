package com.suntek.vdm.gw.welink.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;
import com.suntek.vdm.gw.welink.api.response.GetUsersListNewResponse;

import java.util.List;

public interface WelinkNodeMeetingRoomService {
    /**
     * 从企业通讯录查询会议室信息
     * @param query
     * @param Body
     * @return
     * @throws MyHttpException
     */
    GetUsersListNewResponse getAddressBookRooms(String query, String Body, String token) throws MyHttpException;

    List<GetAddressBookRoomsResponse> getAddressBookRooms(String id, String searchType, String keyWord, String token) throws MyHttpException;

    OrganizationsOld queryOrganizations(String id) throws MyHttpException;
}

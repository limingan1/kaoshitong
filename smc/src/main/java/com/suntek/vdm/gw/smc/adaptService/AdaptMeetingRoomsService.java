package com.suntek.vdm.gw.smc.adaptService;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;

import java.util.List;

public interface AdaptMeetingRoomsService {
    /**
     * 从企业通讯录查询会议室信息
     * @param id
     * @param searchType
     * @param keyWord
     * @param token
     * @return
     * @throws MyHttpException
     */
    String getAddressBookRooms(String id, String searchType, String keyWord, String token) throws MyHttpException;

    String roomsConditions(String id, String keyWord, Integer page, Integer size, String searchType, String token) throws MyHttpException;
}

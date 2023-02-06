package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsComnditionsResp;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.conf.context.SmcMeetingRoomContext;
import com.suntek.vdm.gw.conf.service.MeetingRoomService;
import com.suntek.vdm.gw.conf.service.SmcMeetingRoomService;
import com.suntek.vdm.gw.core.pojo.RemoteToken;
import com.suntek.vdm.gw.core.service.RoutManageService;
import com.suntek.vdm.gw.core.service.VmNodeDataService;
import com.suntek.vdm.gw.core.service.VmNodeTokenManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MeetingRoomServiceImpl extends BaseServiceImpl implements MeetingRoomService {

    @Autowired
    private SmcMeetingRoomContext smcMeetingRoomContext;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private VmNodeTokenManagerService vmNodeTokenManagerService;

    @Override
    public List<GetAddressBookRoomsResponse> getAddressBookRooms(String id, String searchType, String keyWord, String casOrgId, String token, GwId gwId) throws MyHttpException {
      return  smcMeetingRoomContext.get(gwId).getAddressBookRooms(id, searchType, keyWord, getSmcToken(token));
    }

    @Override
    public GetAddressBookRoomsComnditionsResp roomsConditions(String id, String keyWord, Integer page, Integer size, String casOrgId, String token, String searchType, GwId gwId) throws MyHttpException {
        return  smcMeetingRoomContext.get(gwId).roomsConditions(id, keyWord, page, size, searchType, getSmcToken(token));
    }

    @Override
    public OrganizationsOld queryOrganizations(String id, GwId gwId, String token,GwId casOrgGwId) throws MyHttpException{
        SmcMeetingRoomService smcMeetingRoomService = smcMeetingRoomContext.get(gwId);
        return smcMeetingRoomService.queryOrganizations(id,getSmcToken(token),casOrgGwId);
    }

    @Override
    public String queryAddressBookUsers(String token, String orgEntryUuid, String keyWord, String page, String size, String middleUriFilter, GwId gwId) throws MyHttpException {
        if (SystemConfiguration.smcVersionIsV2()) {
            return "{\"content\":[],\"pageable\":{\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"pageSize\":10,\"pageNumber\":0,\"offset\":0,\"paged\":true,\"unpaged\":false},\"totalPages\":1,\"totalElements\":0,\"last\":true,\"first\":true,\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"numberOfElements\":0,\"size\":10,\"number\":0,\"empty\":true}";
        }
        keyWord = keyWord == null ? "" : keyWord;
        SmcMeetingRoomService smcMeetingRoomService = smcMeetingRoomContext.get(gwId);
        return smcMeetingRoomService.queryAddressBookUsers(orgEntryUuid, keyWord, page, size, middleUriFilter, getSmcToken(token));
    }
}

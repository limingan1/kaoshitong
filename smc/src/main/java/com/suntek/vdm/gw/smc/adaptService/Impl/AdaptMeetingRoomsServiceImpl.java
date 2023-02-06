package com.suntek.vdm.gw.smc.adaptService.Impl;

import com.huawei.vdmserver.common.dto.ResponseEntityEx;
import com.huawei.vdmserver.common.vo.ConfVO;
import com.huawei.vdmserver.smc.core.service.SmcConferenceScheduledService;
import com.huawei.vdmserver.smc.core.service.SmcQueryOrganizationService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingRoomsService;
import com.suntek.vdm.gw.smc.adaptService.util.AdaptHttpStateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AdaptMeetingRoomsServiceImpl implements AdaptMeetingRoomsService {
    @Autowired
    @Qualifier("SmcConferenceScheduledService2.0")
    SmcConferenceScheduledService smcConferenceScheduledService;

    @Override
    public String getAddressBookRooms(String id, String searchType, String keyWord, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setKeyword(keyWord);
        confVO.setSearchType(searchType);
        ResponseEntityEx<?> object = smcConferenceScheduledService.editRecurrenceConf(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String roomsConditions(String id, String keyWord, Integer page, Integer size, String searchType, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setKeyword(keyWord);
        confVO.setSearchType(searchType);
        confVO.setPage(page == null ? 0 : page);
        confVO.setPage(confVO.getPage() + 1);
        confVO.setSize(size == null ? 10 : size);
        ResponseEntityEx<?> object = smcConferenceScheduledService.queryMeetingRoomFromEUA(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }
}

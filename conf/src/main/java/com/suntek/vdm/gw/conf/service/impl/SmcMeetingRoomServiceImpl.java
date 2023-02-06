package com.suntek.vdm.gw.conf.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.request.meeting.Organization;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsComnditionsResp;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsDto;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;
import com.suntek.vdm.gw.conf.service.OtherService;
import com.suntek.vdm.gw.conf.service.SmcMeetingRoomService;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.pojo.RemoteToken;
import com.suntek.vdm.gw.core.service.LocalTokenManageService;
import com.suntek.vdm.gw.core.service.VmNodeTokenManagerService;
import com.suntek.vdm.gw.smc.service.SmcNodeMeetingRoomService;
import com.suntek.vdm.gw.smc.service.impl.SmcBaseServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SmcMeetingRoomServiceImpl extends SmcBaseServiceImpl implements SmcMeetingRoomService {
    @Autowired
    SmcNodeMeetingRoomService smcNodeMeetingRoomService;
    @Autowired
    private OtherService otherService;
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private VmNodeTokenManagerService vmNodeTokenManagerService;

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
        return smcNodeMeetingRoomService.getAddressBookRooms(id, searchType, keyWord, token);
    }

    @Override
    public GetAddressBookRoomsComnditionsResp roomsConditions(String id, String keyWord, Integer page, Integer size, String searchType, String token) throws MyHttpException {
        return smcNodeMeetingRoomService.roomsConditions(id, keyWord, page, size, searchType, token);
    }

    @Override
    public OrganizationsOld queryOrganizations(String id, String token, GwId gwId) throws MyHttpException{
        LocalToken localToken = localTokenManageService.get(token);
        log.info("org casOrgId:{},localToken:{}", gwId, localToken);
        if ((id == null || "".equals(id)) && gwId != null && (localToken != null || vmNodeTokenManagerService.get(gwId.getNodeId()) != null)) {
            if (localToken == null) {
                RemoteToken remoteToken = vmNodeTokenManagerService.get(gwId.getNodeId());
                localToken = localTokenManageService.get(remoteToken == null ? "" : remoteToken.getToken());
            }
            LocalToken finalLocalToken = localToken;
            List<Organization> organizations = otherService.getOrganizations(finalLocalToken.getSmcToken());
            log.info("finalLocalToken:{}", finalLocalToken);
            String currentNodeName = organizations.stream().filter(item -> item.getId().equals(finalLocalToken.getOrgId())).collect(Collectors.toList()).get(0).getName();
            List<String> deptIds = Collections.singletonList(id == null ? "" : id);
            log.info("current node name:{}", currentNodeName);
            while (!"root".equals(currentNodeName)) {
                OrganizationsOld organizationsOld = smcNodeMeetingRoomService.queryOrganizations(deptIds, finalLocalToken.getSmcToken());
                if (organizationsOld == null) {
                    return new OrganizationsOld();
                }
                List<OrganizationsDto> result = organizationsOld.getOrganizationResultBeanList();
                log.info("current result list:{}", result);
                if (result != null && !result.isEmpty()) {
                    if (result.stream().anyMatch(item -> item.getOu().equals(currentNodeName))) {
                        OrganizationsDto data = result.stream().filter(item -> item.getOu().equals(currentNodeName)).collect(Collectors.toList()).get(0);
                        if (result.stream().noneMatch(item -> item.getOu().equals("VC"))) {
                            result.add(0, new OrganizationsDto("7649fc39-81ba-4649-a237-5454502e9ff2", "VC", "VC", true));
                        }
                        List<String> orgNameList = data.getOrgNameList();
                        List<String> removeItem = new ArrayList<>();
                        for (String orgName : orgNameList) {
                            if (orgName.equals("VC")) {
                                continue;
                            }
                            if (orgName.equals(currentNodeName)) {
                                break;
                            }
                            removeItem.add(orgName);
                        }
                        for (String index : removeItem) {
                            orgNameList.remove(index);
                        }
                        return organizationsOld;
                    } else {
                        if (result.size() > 1) {
                            if (result.stream().anyMatch(item -> item.getOu().equals("VC"))) {
                                deptIds = Collections.singletonList(result.stream().filter(item -> item.getOu().equals("VC")).collect(Collectors.toList()).get(0).getEntryUuid());
                            } else {
                                deptIds = result.stream().map(OrganizationsDto::getEntryUuid).collect(Collectors.toList());
                            }
                        } else {
                            deptIds = Collections.singletonList(result.get(0).getEntryUuid());
                        }
                    }
                } else {
                    return organizationsOld;
                }
            }
        }
        return smcNodeMeetingRoomService.queryOrganizations(id, token);
    }

    @Override
    public String queryAddressBookUsers(String orgEntryUuid, String keyWord, String page, String size, String middleUriFilter,String token) throws MyHttpException{
        return smcNodeMeetingRoomService.queryAddressBookUsers(orgEntryUuid, keyWord, page, size, middleUriFilter, token);
    }
}

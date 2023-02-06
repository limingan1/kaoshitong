package com.suntek.vdm.gw.api.controller.conf;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsComnditionsResp;
import com.suntek.vdm.gw.common.pojo.response.room.GetAddressBookRoomsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;
import com.suntek.vdm.gw.conf.service.MeetingRoomService;
import com.suntek.vdm.gw.core.annotation.Forward;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/conf-portal")
public class MeetingRoomController {
    @Autowired
    private MeetingRoomService meetingRoomService;

    @Forward
    @GetMapping("/addressbook/rooms")
    public ResponseEntity<String> scheduleConferences(@RequestParam String id,
                                                      @RequestParam String searchType,
                                                      @RequestParam(required = false) String keyWord,
                                                      @RequestParam(required = false) String casOrgId,
                                                      @RequestHeader("Token") String token,
                                                      @RequestHeader(value = "TargetId", required = false) String targetId
    ) {
        try {
            List<GetAddressBookRoomsResponse> response = meetingRoomService.getAddressBookRooms(id, searchType, keyWord, casOrgId, token, GwId.valueOf(targetId));
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @GetMapping("/addressbook/rooms/conditions")
    public ResponseEntity<String> roomsConditions(@RequestParam String id,
                                                  @RequestParam(value = "searchType", required = false) String searchType,
                                                  @RequestParam(value = "keyWord", required = false) String keyWord,
                                                  @RequestParam(value = "casOrgId", required = false) String casOrgId,
                                                  @RequestParam(value = "page", required = false) Integer page,
                                                  @RequestParam(value = "size", required = false) Integer size,
                                                  @RequestHeader("Token") String token,
                                                  @RequestHeader(value = "TargetId", required = false) String targetId) {
        GetAddressBookRoomsComnditionsResp response = null;
        try {
            response = meetingRoomService.roomsConditions(id, keyWord, page, size, casOrgId, token, searchType, GwId.valueOf(targetId));
        } catch (MyHttpException exception) {
            return new ResponseEntity<>(exception.getBody(), HttpStatus.valueOf(exception.getCode()));
        }
        return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
    }

    @GetMapping("/addressbook/organizations/{id}")
    public ResponseEntity<String> queryOrganizations(@PathVariable(value = "id",required = false)String id,
                                                @RequestHeader("Token") String token,
                                                @RequestParam(value = "casOrgId", required = false) String casOrgId,
                                                @RequestHeader(value = "TargetId", required = false) String targetId) {
        GwId gwId = GwId.valueOf(targetId);
        GwId casOrgGwId = GwId.valueOf(casOrgId);
        try {
            OrganizationsOld organizationsOld = meetingRoomService.queryOrganizations(id, gwId, token,casOrgGwId);
            return new ResponseEntity<>(JSON.toJSONString(organizationsOld), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }
    @GetMapping("/addressbook/organizations")
    public ResponseEntity<String> queryAllOrganizations(@RequestHeader("Token") String token,
                                                @RequestParam(value = "casOrgId", required = false) String casOrgId,
                                                @RequestHeader(value = "TargetId", required = false) String targetId) {
        return queryOrganizations(null, token, casOrgId, targetId);
    }

    /**
     * 查询与会人
     */
    @GetMapping("/addressbook/users/conditions")
    public ResponseEntity<String> queryAddressbookUsers(@RequestHeader("Token") String token,
                                                        @RequestParam(value = "orgEntryUuid", required = false) String orgEntryUuid,
                                                        @RequestParam(value = "keyWord", required = false) String keyWord,
                                                        @RequestParam(value = "page", required = false) String page,
                                                        @RequestParam(value = "size", required = false) String size,
                                                        @RequestParam(value = "middleUriFilter", required = false) String middleUriFilter,
                                                        @RequestParam(value = "casOrgId", required = false) String casOrgId
    ) {
        if (casOrgId == null || "".equals(casOrgId)) {
            return new ResponseEntity<>("{\"content\":[],\"pageable\":{\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"pageSize\":10,\"pageNumber\":0,\"offset\":0,\"paged\":true,\"unpaged\":false},\"totalPages\":1,\"totalElements\":0,\"last\":true,\"first\":true,\"sort\":{\"sorted\":false,\"unsorted\":true,\"empty\":true},\"numberOfElements\":0,\"size\":10,\"number\":0,\"empty\":true}", HttpStatus.OK);
        }
        GwId gwId = GwId.valueOf(casOrgId);
        try {
            String result = meetingRoomService.queryAddressBookUsers(token, orgEntryUuid, keyWord, page, size, middleUriFilter, gwId);
            return new ResponseEntity<>(result, HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }
}

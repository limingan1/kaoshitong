package com.suntek.vdm.gw.welink.dispenser;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.suntek.vdm.gw.common.api.request.DurationMeetingRequestEx;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequestEx;
import com.suntek.vdm.gw.common.api.request.ParticipantsControlRequestEx;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CascadeChannelFreeInfo;
import com.suntek.vdm.gw.common.pojo.CascadeChannelNotifyInfo;
import com.suntek.vdm.gw.common.pojo.GetParticipantsDetailInfoResponse;
import com.suntek.vdm.gw.common.pojo.request.*;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.AddCasChannelResp;
import com.suntek.vdm.gw.common.pojo.response.CasConferenceInfosResponse;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.common.pojo.response.room.GetSiteRegiesterStatusResp;
import com.suntek.vdm.gw.common.pojo.response.room.OrganizationsOld;
import com.suntek.vdm.gw.common.pojo.websocket.SubscribeMessage;
import com.suntek.vdm.gw.welink.api.response.GetUsersListNewResponse;
import com.suntek.vdm.gw.welink.enums.UrlRegex;
import com.suntek.vdm.gw.welink.pojo.WelinkConference;
import com.suntek.vdm.gw.welink.service.WelinkCasChannelManageService;
import com.suntek.vdm.gw.welink.service.WelinkMeetingService;
import com.suntek.vdm.gw.welink.service.WelinkNodeMeetingRoomService;
import com.suntek.vdm.gw.welink.service.impl.WelinkMeetingManagerService;
import com.suntek.vdm.gw.welink.util.RequestUtil;
import com.suntek.vdm.gw.welink.websocket.WeLinkWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class WelinkDispenser {
    @Autowired
    private WelinkNodeMeetingRoomService welinkNodeMeetingRoomService;
    @Autowired
    private WelinkMeetingService welinkMeetingService;
    @Autowired
    private WelinkMeetingManagerService welinkMeetingManagerService;
    @Autowired
    private WeLinkWebSocketService weLinkWebSocketService;
    @Autowired
    private WelinkCasChannelManageService welinkCasChannelManageService;
    @Autowired
    private ObjectMapper objectMapper;


    public ResponseEntity<String> WelinkDispenser(String url, Object body, MultiValueMap<String, String> headers, HttpMethod method) throws MyHttpException {
        ResponseEntity<String> responseEntity = null;
        String token = String.valueOf(headers.get("Token").get(0));
        log.info("welink request url: {},  body: {}", url, body);
        URL aURL = null;
        try {
            aURL = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        String path = aURL.getPath();
        boolean needDeal = true;
        String newBody = "";
        if(!(body instanceof String)) {
            newBody = JSON.toJSONString(body, SerializerFeature.DisableCircularReferenceDetect);
        }else{
            newBody = (String) body;
        }
        String query = aURL.getQuery();
        switch (path){
            //获取会场列表
            case "/conf-portal/addressbook/rooms/conditions": {
                GetUsersListNewResponse getUsersListNewResponse = welinkNodeMeetingRoomService.getAddressBookRooms(query, newBody, token);
                responseEntity = new ResponseEntity(JSONObject.toJSONString(getUsersListNewResponse), HttpStatus.OK);
                needDeal = false;
                break;
            }
            //召集会议
            case "/conf-portal/conferences": {
                JSONObject respJson = welinkMeetingService.ScheduleConf(newBody, token);
                responseEntity = new ResponseEntity(respJson.toJSONString(), HttpStatus.OK);
                needDeal = false;
                break;
            }
            case "/conf-portal/cascade/subscribe": {
                SubscribeMessage subscribeMessage = JSON.parseObject(newBody, SubscribeMessage.class);
                subscribeMessage.setTargetGwId(welinkMeetingManagerService.getWelinkNodeData().getGwId());
                String data = subscribeMessage.getInfo().getData();
                Map<String,String> map = JSON.parseObject(data, Map.class);
                String conferenceId = map.get("conferenceId");
                //订阅welink
                if(HttpMethod.POST.equals(method)){
                    WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConferenceMap().get(conferenceId);
                    String regex = UrlRegex.SUBSCRIBE_PARTICIPANT_CONTROL.getRegex();
                    if(subscribeMessage.getBackDestination().matches(regex)) {
                        welinkConference.setConferenceSubscribeMessage(subscribeMessage);
                    }else{
                        welinkConference.setSubscribeMessage(subscribeMessage);
                    }
                    weLinkWebSocketService.connect(conferenceId);
                }else if(HttpMethod.DELETE.equals(method)){
                    //取消订阅
                    weLinkWebSocketService.disconnect(conferenceId);
                }
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            case "/conf-portal/conferences/conditions": {
                GetConditionsMeetingRequest subscribeMessage = JSON.parseObject(newBody, GetConditionsMeetingRequest.class);
                GetConditionsMeetingResponse getConditionsMeetingResponse = welinkMeetingService.getWelinkMeetingConditions(subscribeMessage, query, token);
                responseEntity = new ResponseEntity(JSONObject.toJSONString(getConditionsMeetingResponse), HttpStatus.OK);
                needDeal = false;
                break;
            }
            case "/conf-portal/cascade/channel/free": {
                CascadeChannelFreeInfo cascadeChannelFreeInfo = JSON.parseObject(newBody, CascadeChannelFreeInfo.class);
                welinkCasChannelManageService.freeCascadeChannel(cascadeChannelFreeInfo);
                needDeal = false;
                break;
            }
            case "/conf-portal/cascade/channel/notify": {
//          改名主通道
                CascadeChannelNotifyInfo cascadeChannelNotifyInfo =  JSON.parseObject(newBody, CascadeChannelNotifyInfo.class);
                welinkMeetingService.reNameMainChannel(cascadeChannelNotifyInfo);
                needDeal = false;
                break;
            }
            case "/conf-portal/configs/search/findareatreeconfig": {
                Map<String, String[]> values = new HashMap<>();
                try {
                    RequestUtil.parseParameters(values, query, "utf8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                String configName = values.get("configName")[0];
                FindAreaTreeConfigReq result = new FindAreaTreeConfigReq("8ab9e3c469ce04e50169ce058e5d1234", configName, false);
                responseEntity = new ResponseEntity(JSONObject.toJSONString(result), HttpStatus.OK);
                return responseEntity;
            }
            case "/conf-portal/conferences/register/status/conditions": {
                GetSiteRegiesterStatusReq getSiteRegiesterStatusReq = JSONObject.parseObject(newBody, GetSiteRegiesterStatusReq.class);
                List<GetSiteRegiesterStatusResp> getSiteRegiesterStatusResps = new ArrayList<>();
                if(getSiteRegiesterStatusReq.getUris() != null){
                    for(String uri: getSiteRegiesterStatusReq.getUris()){
                        GetSiteRegiesterStatusResp getSiteRegiesterStatusResp = new GetSiteRegiesterStatusResp(uri, false, true,false,false);
                        getSiteRegiesterStatusResps.add(getSiteRegiesterStatusResp);
                    }
                }
                responseEntity = new ResponseEntity(JSONObject.toJSONString(getSiteRegiesterStatusResps), HttpStatus.OK);
                return responseEntity;
            }
            case "/conf-portal/cascade/addCasChannel": {
                AddCasChannelReq addCasChannelReq = JSONObject.parseObject(newBody, AddCasChannelReq.class);
                AddCasChannelResp addCasChannelResp = welinkMeetingService.addCasChannel(addCasChannelReq, token);
                responseEntity = new ResponseEntity(JSONObject.toJSONString(addCasChannelResp), HttpStatus.OK);
                return responseEntity;
            }
            case "/conf-portal/addressbook/organizations": {
                OrganizationsOld organizationsOld = welinkNodeMeetingRoomService.queryOrganizations("1");
                return new ResponseEntity(JSONObject.toJSONString(organizationsOld), HttpStatus.OK);
            }
        }
        if (needDeal) {
            String regex = UrlRegex.MEETING_DETAIL.getRegex();
            if (path.matches(regex)) {
                //查询会议详情
                String conferenceId = getPathVar(regex, path,1)[0];
                GetMeetingDetailResponse response = welinkMeetingService.getWelinkMeetingDetail(conferenceId, token);
                responseEntity = new ResponseEntity(JSONObject.toJSONString(response), HttpStatus.OK);
                return responseEntity;
            }
            regex = UrlRegex.PARTICIPANTS_LIST.getRegex();
            if (path.matches(regex)) {
                GetParticipantsRequest getParticipantsRequest = JSONObject.parseObject(newBody, GetParticipantsRequest.class);
                if(getParticipantsRequest != null && getParticipantsRequest.getHandUp()!= null && getParticipantsRequest.getHandUp()){
                    GetParticipantsResponse response = new GetParticipantsResponse();
                    response.setFirst(true);
                    response.setLast(true);
                    response.setTotalPages(1);
                    response.setTotalElements(0);
                    response.setNumber(1);
                    response.setNumberOfElements(0);
                    response.setContent(new ArrayList<>());
                    responseEntity = new ResponseEntity(JSONObject.toJSONString(response), HttpStatus.OK);
                    return responseEntity;
                }
                //查询会场列表
                String conferenceId = getPathVar(regex, path,1)[0];
                GetParticipantsResponse res = welinkMeetingService.getWelinkParticipants(conferenceId);
                String result = "";
                try {
                    result = objectMapper.writeValueAsString(res);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                responseEntity = new ResponseEntity(result, HttpStatus.OK);
                return responseEntity;
            }
            regex = UrlRegex.PARTICIPANT_DETAIL.getRegex();
            if (path.matches(regex)) {
                //查询会场详情
                String[] pathVarArray = getPathVar(regex, path, 2);
                String conferenceId = pathVarArray[0];
                String participantId = pathVarArray[1];
                GetParticipantsDetailInfoResponse res = welinkMeetingService.getParticipantsDetailInfo(conferenceId,participantId);
                responseEntity = new ResponseEntity(JSONObject.toJSONString(res), HttpStatus.OK);
                return responseEntity;
            }
            ///      /conf-portal/online/conferences/{conferenceId}/participants/status
//            批量会操作
            regex = UrlRegex.PARTICIPANTS_CONTROL.getRegex();
            if (path.matches(regex)) {
                //管控
                String conferenceId = getPathVar(regex, path, 1)[0];
                List<JSONObject> participantsControlRequestExs = JSON.parseObject(newBody, List.class);
                welinkMeetingService.participantsControl(participantsControlRequestExs,conferenceId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
//            单个会场操作
            regex = UrlRegex.PARTICIPANT_CONTROL.getRegex();
            if (path.matches(regex)) {
                String[] pathVar = getPathVar(regex, path, 2);
                String conferenceId = pathVar[0];
                String participantId = pathVar[1];
                ParticipantsControlRequestEx requestEx = JSON.parseObject(newBody, ParticipantsControlRequestEx.class);
                if (conferenceId == null && participantId == null && !requestEx.isAutoCascadeChannel()) {
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
                JSONObject jsonObject = welinkMeetingService.participantControl(conferenceId, participantId, requestEx);
                if (jsonObject != null && !jsonObject.isEmpty()) {
                    return new ResponseEntity<>(jsonObject.toJSONString(), HttpStatus.OK);
                }
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }

            regex = UrlRegex.ADD_DEL_PARTICIPANTS.getRegex();
            if (path.matches(regex)) {
                //添加/删除会场
                String conferenceId = getPathVar(regex, path, 1)[0];
                if (HttpMethod.POST.equals(method)) {
                    List<JSONObject> participantsControlRequestExs = JSON.parseObject(newBody, List.class);
                    welinkMeetingService.addParticipants(participantsControlRequestExs, conferenceId, token,query);
                } else if (HttpMethod.DELETE.equals(method)){
                    List<String> participantsControlRequestExs = JSON.parseObject(newBody, List.class);
                    welinkMeetingService.delAttendees(participantsControlRequestExs, conferenceId);
                }
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            //会议控制
            regex = UrlRegex.MEETING_CONTROL.getRegex();
            if (path.matches(regex)) {
                String conferenceId = getPathVar(regex, path, 1)[0];
                MeetingControlRequestEx requestEx = JSON.parseObject(newBody, MeetingControlRequestEx.class);
                welinkMeetingService.meetingControl(conferenceId, requestEx);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            regex = UrlRegex.MEETING_DURATION.getRegex();
            if (path.matches(regex)) {
                String conferenceId = getPathVar(regex, path, 1)[0];
                DurationMeetingRequestEx requestEx = JSON.parseObject(newBody, DurationMeetingRequestEx.class);
                welinkMeetingService.durationMeeting(conferenceId, requestEx);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            regex = UrlRegex.DELETE_MEETING.getRegex();
            if (path.matches(regex)) {
                String conferenceId = getPathVar(regex, path, 1)[0];
                welinkMeetingService.stopMeeting(conferenceId);
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            regex = UrlRegex.QUERY_ADDRESS_BOOK.getRegex();
            if (path.matches(regex)) {
                String id = getPathVar(regex, path, 1)[0];
                OrganizationsOld organizationsResp = welinkNodeMeetingRoomService.queryOrganizations(id);
                return new ResponseEntity(JSONObject.toJSONString(organizationsResp), HttpStatus.OK);
            }
            regex = UrlRegex.GET_ONE_CONFERENCE.getRegex();
            if(path.matches(regex)){
                String conferenceId = getPathVar(regex, path, 1)[0];
                if (HttpMethod.GET.equals(method)) {
                    JSONObject jsonObject = welinkMeetingService.getOne(conferenceId, token);
                    return new ResponseEntity(jsonObject.toJSONString(), HttpStatus.OK);
                }else if(HttpMethod.DELETE.equals(method)){
                    welinkMeetingService.deleteMeeting(conferenceId, token);
                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);
                }
            }
            regex = UrlRegex.CAS_CONFERENCE_INFOS.getRegex();
            if(path.matches(regex)){
                String conferenceId = getPathVar(regex, path, 1)[0];
                CasConferenceInfosResponse casConferenceInfosResponse = welinkMeetingService.getCasConferenceInfos(conferenceId);
                return new ResponseEntity(JSONObject.toJSONString(casConferenceInfosResponse), HttpStatus.OK);
            }
        }

        return responseEntity;
    }

    /**
     *
     * @param regex 正则表达式
     * @param url   url
     * @param paramCount url中参数的个数
     * @return 参数数组，从前往后排序
     */
    public String[] getPathVar(String regex, String url,int paramCount) {
        String[] params = new String[paramCount];
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(url);
        if (matcher.find()) {
            for (int i = 1; i <= paramCount; i++) {
                params[i - 1] = matcher.group(i);
            }
            return params;
        }
        return null;
    }
}

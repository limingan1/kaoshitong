package com.suntek.vdm.gw.welink.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.api.request.DurationMeetingRequestEx;
import com.suntek.vdm.gw.common.api.request.MeetingControlRequestEx;
import com.suntek.vdm.gw.common.api.request.ParticipantsControlRequestEx;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.CascadeChannelNotifyType;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.*;
import com.suntek.vdm.gw.common.pojo.request.AddCasChannelReq;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.AddCasChannelResp;
import com.suntek.vdm.gw.common.pojo.response.CasConferenceInfosResponse;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetMeetingDetailResponse;
import com.suntek.vdm.gw.common.pojo.response.meeting.GetParticipantsResponse;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.common.util.UtcTimeUtil;
import com.suntek.vdm.gw.welink.api.pojo.*;
import com.suntek.vdm.gw.welink.api.request.*;
import com.suntek.vdm.gw.welink.api.response.GetMeetingListResponse;
import com.suntek.vdm.gw.welink.api.response.QueryUserResultDTO;
import com.suntek.vdm.gw.welink.api.response.ScheduleMeetingResponse;
import com.suntek.vdm.gw.welink.api.response.UserVmrDTO;
import com.suntek.vdm.gw.welink.api.service.WeLinkMeetingControlService;
import com.suntek.vdm.gw.welink.api.service.WeLinkMeetingManagementService;
import com.suntek.vdm.gw.welink.pojo.WelinkConference;
import com.suntek.vdm.gw.welink.pojo.WelinkNodeData;
import com.suntek.vdm.gw.welink.service.WelinkCasChannelManageService;
import com.suntek.vdm.gw.welink.service.WelinkMeetingService;
import com.suntek.vdm.gw.welink.util.RequestUtil;
import com.suntek.vdm.gw.welink.websocket.WeLinkWebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
@Slf4j
public class WelinkMeetingServiceImpl implements WelinkMeetingService {
    @Autowired
    private WeLinkMeetingManagementService weLinkMeetingManagementService;
    @Autowired
    private WeLinkTokenManageServiceImpl weLinkTokenManageService;
    @Autowired
    private WelinkMeetingManagerService welinkMeetingManagerService;
    @Autowired
    private WeLinkWebSocketService weLinkWebSocketService;
    @Autowired
    private WeLinkMeetingControlService weLinkMeetingControlService;
    @Autowired
    private WelinkCasChannelManageService welinkCasChannelManageService;

    private static final String FORMATSTRINGTHREE = "%03d";
    private static final int MAX_CASCADENUM = 64;

    @Override
    public JSONObject ScheduleConf(String request, String token) throws MyHttpException {
        JSONObject body = JSONObject.parseObject(request);
        String smcAccessCode = body.getString("accessCode");
        int cascadeNum = body.getIntValue("cascadeNum");
        JSONObject conference = body.getJSONObject("conference");
        String subject = conference.getString("subject");
        String scheduleStartTime = conference.getString("scheduleStartTime");
        Integer duration = conference.getInteger("duration");
        String conferenceTimeType = conference.getString("conferenceTimeType");
        if(duration > 1440 || duration == 0){
            duration = 1440;
        }
        JSONArray participants = body.getJSONArray("participants");
        List<Attendee> attendees = dealAttends(participants, smcAccessCode,cascadeNum);

        ScheduleMeetingRequest scheduleMeetingRequest = new ScheduleMeetingRequest();
        scheduleMeetingRequest.setMediaTypes("Voice,Video,Data");
        scheduleMeetingRequest.setStartTime(scheduleStartTime);
        scheduleMeetingRequest.setLength(duration);
        scheduleMeetingRequest.setConferenceType(0);
        scheduleMeetingRequest.setSubject(subject);
        scheduleMeetingRequest.setAttendees(attendees);
        if ("INSTANT_CONFERENCE".equals(conferenceTimeType)) {
//            scheduleMeetingRequest.setStartTime(UtcTimeUtil.getUTCTimeStr());
            scheduleMeetingRequest.setStartTime(null);
        }
        boolean isVmr = body.getBooleanValue("isVmr");
        if(isVmr){
            UserVmrDTO vmrDTO = getVmrDTO(token);
            if(vmrDTO != null){
                scheduleMeetingRequest.setVmrFlag(1);
                scheduleMeetingRequest.setVmrID(vmrDTO.getId());
            }else {
                //返回失败，召集vmr会议失败
                throw new MyHttpException(409, GwErrorCode.NO_FREE_VMRID.toString());
            }
        }
        ScheduleMeetingResponse scheduleMeetingResponse = weLinkMeetingManagementService.scheduleMeeting(scheduleMeetingRequest,token);
        JSONObject respJson = new JSONObject();
        JSONObject multiConferenceService = new JSONObject();
        String conferenceID = scheduleMeetingResponse.getConferenceID();
        multiConferenceService.put("accessCode", conferenceID);
        JSONObject respConference = new JSONObject();
        respConference.put("subject", scheduleMeetingResponse.getSubject());
        respConference.put("id", conferenceID);
        //主持人密码
        List<PasswordEntry> passwordEntrys = scheduleMeetingResponse.getPasswordEntry();
        if(passwordEntrys != null){
            for(PasswordEntry passwordEntry : passwordEntrys){
                if("chair".equals(passwordEntry.getConferenceRole())){
                    respConference.put("chairmanPassword", passwordEntry.getPassword());
                }else {
                    respConference.put("guestPassword", passwordEntry.getPassword());
                }
            }
        }

        respJson.put("multiConferenceService", multiConferenceService);
        respJson.put("conference", respConference);
        WelinkNodeData welinkNodeData = welinkMeetingManagerService.getWelinkNodeData();
        String accessCode = welinkNodeData.getAreaCode() + conferenceID;
        respJson.put("accessCode", accessCode);
        String welinkNodeName = welinkNodeData.getName();
        respJson.put("nodeName", welinkNodeName);
        respJson.put("smcVersionType", SmcVersionType.Welink);
        welinkMeetingManagerService.createWelinkConference(conferenceID,smcAccessCode,welinkNodeName, accessCode,cascadeNum);
        weLinkWebSocketService.connect(conferenceID);
        return respJson;
    }

    private UserVmrDTO getVmrDTO(String token) throws MyHttpException {
        String vmrConfId = welinkMeetingManagerService.getWelinkNodeData().getVmrConfId();
        QueryUserResultDTO queryUserResultDTO = weLinkMeetingManagementService.getUserDcsMenber(token);
        List<UserVmrDTO> vmrList = queryUserResultDTO.getVmrList();
        if(vmrConfId != null) {
            //转换vmrId
            for (UserVmrDTO userVmrDTO : vmrList) {
                if (vmrConfId.equals(userVmrDTO.getVmrId())) {
                    //检测vmr会议Id能不能用
                    boolean isUsed = isVmrUsed(token, userVmrDTO);
                    if(!isUsed){
                        return userVmrDTO;
                    }
                }
            }
        }

        //获取第一个可用的vmrId
        for(UserVmrDTO userVmrDTO : vmrList){
            Integer status = userVmrDTO.getStatus();
            if(status != null && status == 0){
                boolean isUsed = isVmrUsed(token, userVmrDTO);
                if(!isUsed){
                    return userVmrDTO;
                }
            }
        }
        log.error("VmrDTO get error.");
        return null;
    }

    private boolean isVmrUsed(String token, UserVmrDTO userVmrDTO) throws MyHttpException {
        GetMeetingListRequest getMeetingListRequest = new GetMeetingListRequest(userVmrDTO.getVmrId(), 1, 100);
        GetMeetingListResponse getMeetingListResponse = weLinkMeetingManagementService.getMeetingList(getMeetingListRequest,token);
        if(getMeetingListResponse != null && getMeetingListResponse.getData() != null && !getMeetingListResponse.getData().isEmpty()){
            for(ConferenceInfo conferenceInfo: getMeetingListResponse.getData()){
                if(conferenceInfo.getVmrConferenceID().equals(userVmrDTO.getVmrId())){
                    return true;
                }
            }
        }
        return false;
    }

    private List<Attendee> dealAttends(JSONArray participants, String accessCode,int cascadeNum){
        List<Attendee> attendees = new ArrayList<>();
        if(participants == null || participants.size() == 0){
            return attendees;
        }
        for(int i = 0; i< participants.size(); i++){
            JSONObject participant = participants.getJSONObject(i);
            String uri = participant.getString("uri");
            String name = participant.getString("name");
            Attendee attendee = new Attendee(uri, name);
            attendees.add(attendee);
        }
        //添加welink级联通道
        for (int i = 0; i < cascadeNum; i++) {
            String callerNumber = getWelinkUriByNumber(accessCode, i, cascadeNum);
            Attendee attend = new Attendee(callerNumber, "welink(" + (i + 1)+")");
            attend.setCascadeChannel(i != 0);
            attend.setIsAutoInvite(0);
            attend.setUri(callerNumber);
            attendees.add(attend);
        }
        return attendees;
    }
    //得到welink增加级联后缀标识的结果,0101234-->0101234001，第一条大于64
    @Override
    public String getWelinkUriByNumber(String smcAccess, Integer num, Integer cascadeNum){
        String SmcAccessSuffix = "";
        if(smcAccess == null || cascadeNum == null){
            return SmcAccessSuffix;
        }
        if(cascadeNum <= 1){
            return smcAccess;
        }
        if(num == 0){
            SmcAccessSuffix = smcAccess+String.format(FORMATSTRINGTHREE,MAX_CASCADENUM+cascadeNum);
        }else{
            SmcAccessSuffix = smcAccess+String.format(FORMATSTRINGTHREE,num);
        }
        return SmcAccessSuffix;
    }

    @Override
    public GetMeetingDetailResponse getWelinkMeetingDetail(String conferenceId, String token) throws MyHttpException {
        GetMeetingDetailRequest getMeetingDetailRequest = new GetMeetingDetailRequest();
        getMeetingDetailRequest.setConferenceID(conferenceId);
        getMeetingDetailRequest.setLimit(500);
        com.suntek.vdm.gw.welink.api.response.GetMeetingDetailResponse getMeetingDetailResponse =
                weLinkMeetingManagementService.getMeetingDetail(getMeetingDetailRequest, token);

        GetMeetingDetailResponse res = new GetMeetingDetailResponse();
        res.setSmcVersion("welink");
        ConferenceUiParam conferenceUiParam = new ConferenceUiParam();
        WelinkNodeData welinkNodeData = welinkMeetingManagerService.getWelinkNodeData();
        String accessCode = welinkNodeData.getAreaCode() + getMeetingDetailResponse.getConferenceData().getConferenceID();
        conferenceUiParam.setAccessCode(accessCode);
        conferenceUiParam.setSubject(getMeetingDetailResponse.getConferenceData().getSubject());
        conferenceUiParam.setId(getMeetingDetailResponse.getConferenceData().getConferenceID());
        conferenceUiParam.setTotalParticipantNum(getMeetingDetailResponse.getData().getCount());
        conferenceUiParam.setIsVmr(getMeetingDetailResponse.getConferenceData().getVmrFlag() == 1);
        res.setConferenceUiParam(conferenceUiParam);
        ConferenceState conferenceState = new ConferenceState();
        conferenceState.setConferenceId(conferenceId);
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if(welinkConference != null) {
            conferenceState.setPresenterId(welinkConference.getSiteShare());
        }
        res.setConferenceState(conferenceState);
        return res;
    }

    @Override
    public GetConditionsMeetingResponse getWelinkMeetingConditions(GetConditionsMeetingRequest getConditionsMeetingRequest, String query, String token) throws MyHttpException {
        Map<String, String[]> values = new HashMap<>();
        try {
            RequestUtil.parseParameters(values, query, "utf8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        GetConditionsMeetingResponse getConditionsMeetingResponse = new GetConditionsMeetingResponse();
        if(getConditionsMeetingRequest.getCasConfId() != null){
            String conferenceId = getConditionsMeetingRequest.getCasConfId();
            WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
            if(welinkConference == null){
                int areaCodeLength = welinkMeetingManagerService.getWelinkNodeData().getAreaCode().length();
                String conferenceID = getConditionsMeetingRequest.getCasConfId();
                if(getConditionsMeetingRequest.getCasConfId().length() > areaCodeLength){
                    conferenceID = getConditionsMeetingRequest.getCasConfId().substring(areaCodeLength);
                }
                GetMeetingDetailRequest getMeetingDetailRequest = new GetMeetingDetailRequest();
                getMeetingDetailRequest.setConferenceID(conferenceID);
                getMeetingDetailRequest.setLimit(500);
                com.suntek.vdm.gw.welink.api.response.GetMeetingDetailResponse getMeetingDetailResponse = null;
                try{
                    getMeetingDetailResponse = weLinkMeetingManagementService.getMeetingDetail(getMeetingDetailRequest, token);
                }catch (MyHttpException exception){
                    log.error("get MeetingDetail failed.");
                }
                int max = 0;
                if(getMeetingDetailResponse != null){
                    String subject = getMeetingDetailResponse.getConferenceData().getSubject();
                    PageParticipant data = getMeetingDetailResponse.getData();
                    List<ParticipantInfo> participantInfos = data.getData();
                    for(ParticipantInfo participantInfo: participantInfos) {
                        String uri = participantInfo.getParticipantID();
                        if(getConditionsMeetingRequest.getSmcAccessCode() == null){
                            break;
                        }
                        if (!uri.startsWith(getConditionsMeetingRequest.getSmcAccessCode())) {
                            continue;
                        }
                        String index = uri.substring(getConditionsMeetingRequest.getSmcAccessCode().length());
                        if("".equals(index)){
                            max = 1;
                        }else{
                            Integer i = Integer.parseInt(index);
                            if (i > 64) {
                                max = i - 64;
                            }
                        }
                    }
                }
                welinkMeetingManagerService.createWelinkConference(conferenceID,getConditionsMeetingRequest.getSmcAccessCode(),
                        welinkMeetingManagerService.getWelinkNodeData().getName(), getConditionsMeetingRequest.getCasConfId(),max);
                welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceID);
                getConditionsMeetingResponse.setContent(Collections.singletonList(welinkConference.toScheduleConfBrief()));
                getConditionsMeetingResponse.setSize(1);
            }else {
                getConditionsMeetingResponse.setContent(Collections.singletonList(welinkConference.toScheduleConfBrief()));
                getConditionsMeetingResponse.setSize(1);
            }
            getConditionsMeetingResponse.setSize(1);
            getConditionsMeetingResponse.setEmpty(false);
            getConditionsMeetingResponse.setFirst(true);
            getConditionsMeetingResponse.setLast(true);
            getConditionsMeetingResponse.setTotalPages(1);
            getConditionsMeetingResponse.setTotalElements(1);
            return getConditionsMeetingResponse;
        }
        GetMeetingListRequest getMeetingListRequest = new GetMeetingListRequest(getConditionsMeetingRequest.getKeyword(), Integer.parseInt(values.get("page")[0]), Integer.parseInt(values.get("size")[0]));
        GetMeetingListResponse getMeetingListResponse = weLinkMeetingManagementService.getMeetingList(getMeetingListRequest, token);
        WelinkNodeData welinkNodeData = welinkMeetingManagerService.getWelinkNodeData();
        GetConditionsMeetingResponse getConditionsMeetingResponse1 = getMeetingListResponse.toGetConditionsMeetingResponse(welinkNodeData.getAreaCode());
        getConditionsMeetingResponse1.setNumber(Integer.parseInt(values.get("page")[0]));
        getConditionsMeetingResponse1.setSize(Integer.parseInt(values.get("size")[0]));
        return getConditionsMeetingResponse1;
    }

    @Override
    public GetParticipantsResponse getWelinkParticipants(String conferenceId) {
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        GetParticipantsResponse res = new GetParticipantsResponse();
        List<ParticipantDetail> content = new ArrayList<>();
        res.setLast(true);
        res.setFirst(true);
        res.setNumber(1);
        res.setTotalPages(1);
        if (welinkConference == null) {
            res.setContent(new ArrayList<>());
            res.setSize(0);
            res.setEmpty(true);
            return res;
        }
        Map<String, ParticipantInfo> allParticipantMap = welinkConference.getAllParticipantMap();
        MultiPicInfo multiPicInfo = null;
        String broadcastUri = welinkConference.getBroadcastUri();
        if(StringUtils.isNotEmpty(broadcastUri)){
            ParticipantInfo participantInfo = allParticipantMap.get(broadcastUri);
            CascadeChannelInfo cascadeChannelInfo = welinkConference.getCascadeChannelInfoMap().get(broadcastUri);
            if(cascadeChannelInfo == null){
                multiPicInfo = new MultiPicInfo();
                multiPicInfo.setPicNum(1);
                List<SubPic> subPicList = new ArrayList<>();
                SubPic subPic = new SubPic();
                subPic.setName(participantInfo.getName());
                subPic.setUri(broadcastUri);
                subPic.setParticipantId(broadcastUri);
                subPic.setStreamNumber(0);
                subPicList.add(subPic);
                multiPicInfo.setSubPicList(subPicList);
            }
        }
        if(null == multiPicInfo){
            multiPicInfo = new MultiPicInfo();
            multiPicInfo.setPicNum(2);
            List<SubPic> subPicList = new ArrayList<>();
            SubPic subPic = new SubPic("", "", "", 0);
            subPicList.add(subPic);
            subPicList.add(subPic);
            multiPicInfo.setSubPicList(subPicList);
        }

        MultiPicInfo finalMultiPicInfo = multiPicInfo;
        MultiPicInfo offlineMultiPicInfo = multiPicInfo;
        allParticipantMap.forEach((uri, participantInfo) -> {
            ParticipantDetail detail = new ParticipantDetail();
            ParticipantGeneralParam participantParam = new ParticipantGeneralParam();
            CascadeChannelInfo cascadeChannelInfo = welinkConference.getCascadeChannelInfoMap().get(uri);
            String name = participantInfo.getName();
            if(cascadeChannelInfo != null){
                participantParam.setVdcMarkCascadeParticipant(cascadeChannelInfo.getBaseInfo().toString());
                if(cascadeChannelInfo.getIndex() == 0){
                    name = "welink(1)";
                }
            }
            participantParam.setId(uri);
            participantParam.setName(name);
            participantParam.setType(2);//0 1 2
            participantParam.setEncodeType("ENCODE_DECODE");
            participantParam.setUri(uri);
            detail.setGeneralParam(participantParam);
            ParticipantState participantState = new ParticipantState();
            participantState.setParticipantId(uri);
            Boolean isOnline = "0".equalsIgnoreCase(participantInfo.getState());
            participantState.setOnline(isOnline);
            participantState.setCalling("1".equalsIgnoreCase(participantInfo.getState()));
            participantState.setVoice(participantInfo.getVideo());
            participantState.setMute(participantInfo.getMute());
            if(isOnline){
                participantState.setMultiPicInfo(finalMultiPicInfo);
            }else {
                offlineMultiPicInfo.setPicNum(1);
                offlineMultiPicInfo.setSubPicList(Collections.singletonList(new SubPic("", "", "", 0)));
                participantState.setMultiPicInfo(offlineMultiPicInfo);
            }
            detail.setState(participantState);
            content.add(detail);
        });
        res.setContent(content);
        res.setEmpty(!res.exist());
        res.setSize(content.size());
        return res;
    }

    @Override
    public GetParticipantsDetailInfoResponse getParticipantsDetailInfo(String conferenceId, String participantId) {
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        GetParticipantsDetailInfoResponse res = new GetParticipantsDetailInfoResponse();
        if (welinkConference == null) {
            return res;
        }
        Map<String, ParticipantInfo> allParticipantMap = welinkConference.getAllParticipantMap();
        ParticipantInfo participantInfo = allParticipantMap.get(participantId);
        res.setName(participantInfo.getName());
        res.setUri(participantInfo.getUri());
        res.setRate(1920);
        res.setIpProtocolType(1);
        return res;
    }

    @Override
    public void participantsControl(List<JSONObject> participantsList, String conferenceId) throws MyHttpException {
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (welinkConference == null) {
            return ;
        }
        Map<String, String> uriToPid = welinkConference.getURI_TO_PID_MAP();
        AddParticipantsRequest addParticipantsRequest = null;
        DelParticipantsRequest delParticipantsRequest  = null;
        for (JSONObject item : participantsList) {
            ParticipantsControlRequestEx participant = item.toJavaObject(ParticipantsControlRequestEx.class);
            String uri = participant.getId();
            String participantId = uriToPid.get(uri);
            if (participant.getIsMute() != null) {
                SetMuteRequest request = new SetMuteRequest(participant.getIsMute() ? 1 : 0, null);
                weLinkMeetingControlService.setMute(conferenceId, participantId, request, welinkConference.getConferenceToken());
            }
            if (participant.getIsOnline() != null) {
                if (participant.getIsOnline()) {
                    //呼叫
                    ParticipantInfo participantInfo = welinkConference.getParticipantInfo(uri);
                    if(participantInfo == null) {
                        participantInfo = new ParticipantInfo(uri, uri);
                    }
                    if(addParticipantsRequest == null){
                        addParticipantsRequest = new AddParticipantsRequest();
                        addParticipantsRequest.setAttendees(new ArrayList<Attendee>());
                    }
                    addParticipantsRequest.getAttendees().add(new Attendee(participantInfo));
                } else {
                    //挂断
                    if(participantId != null){
                        if(delParticipantsRequest == null){
                            delParticipantsRequest = new DelParticipantsRequest();
                            delParticipantsRequest.setBulkHangUpParticipants(new ArrayList<>());
                        }
                        delParticipantsRequest.getBulkHangUpParticipants().add(participantId);
                    }
                }
            }
            if(participant.getMultiPicInfo() != null){
                ParticipantsControlRequestEx participantsControlRequestEx = new ParticipantsControlRequestEx();
                participantsControlRequestEx.setId(uri);
                participantsControlRequestEx.setMultiPicInfo(participant.getMultiPicInfo());
                participantControl(conferenceId, uri, participantsControlRequestEx);
            }
        }
        //呼叫
        if(addParticipantsRequest != null){
            weLinkMeetingControlService.addParticipants(conferenceId,addParticipantsRequest,welinkConference.getConferenceToken());
        }
        //挂断
        if(delParticipantsRequest != null){
            weLinkMeetingControlService.delParticipants(conferenceId,delParticipantsRequest, welinkConference.getConferenceToken());
        }
    }

    @Override
    public JSONObject participantControl(String conferenceId, String uri, ParticipantsControlRequestEx requestEx) throws MyHttpException {
        CasChannelParameter watchCasChannel = CasChannelParameter.valueOf(uri);
        AllocateCasChannelInfo allocateCasChannelInfo = null;
        boolean reuse = false;
        JSONObject jsonObject = new JSONObject();
        if(watchCasChannel != null){
//          级联通道分配
            String firstParticipantId = requestEx.getMultiPicInfo().getFirstParticipantId();
            //todo
            allocateCasChannelInfo = welinkCasChannelManageService.allocateCasChannel(watchCasChannel.getConfId(),watchCasChannel, requestEx.getWatchMeetingControlType(), firstParticipantId);
            if(allocateCasChannelInfo == null){
                log.info("[级联通道] 上级级联通道分配失败 id:{} participantId:{} meetingControlType:{}", conferenceId, firstParticipantId, requestEx.getWatchMeetingControlType().name());
                throw new MyHttpException(409, GwErrorCode.CASCADE_CHANNEL_ALLOCATION_FAILED.toString());
            }
            log.info("allocateCasChannelInfo: {}", allocateCasChannelInfo);
            jsonObject.put("index", allocateCasChannelInfo.getCascadeChannelInfo().getIndex());
            uri = allocateCasChannelInfo.getCascadeChannelInfo().getParticipantId();
        }
        WelinkConference conference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (conference == null) {
            return jsonObject;
        }
        String participantId = conference.getURI_TO_PID_MAP().get(uri);
        if(participantId == null){
            log.info("getURI_TO_PID_MAP: {}", conference.getURI_TO_PID_MAP());
        }
        if (requestEx.getIsOnline() != null) {
            if (requestEx.getIsOnline()) {
                //呼叫
                ParticipantInfo participantInfo = conference.getParticipantInfo(uri);
                if(participantInfo == null) {
                    participantInfo = new ParticipantInfo(uri, uri);
                }
                AddParticipantsRequest request = new AddParticipantsRequest();
                request.setAttendees(Collections.singletonList(new Attendee(participantInfo)));
                weLinkMeetingControlService.addParticipants(conferenceId,request,conference.getConferenceToken());
            } else {
                //挂断
                if(participantId == null){
                    return null;
                }
                DelParticipantsRequest participantReq = new DelParticipantsRequest();
                participantReq.setBulkHangUpParticipants(Collections.singletonList(participantId));
                weLinkMeetingControlService.delParticipants(conferenceId,participantReq, conference.getConferenceToken());
            }
        }
        if(requestEx.getIsMute() != null){
            if(participantId != null){
                String url = "/mmc/control/conferences/participants/mute?conferenceID=" + conferenceId + "&participantID=" + participantId;
                String body = "{\"isMute\": " + (requestEx.getIsMute() ? 1 : 0) + "}";
                weLinkMeetingManagementService.participantsControl(url, body, conference.getConferenceToken());
            }
        }
        if(requestEx.getMultiPicInfo() != null){
            MultiPicInfo multiPicInfo = requestEx.getMultiPicInfo();
            String viewedUri = multiPicInfo.getFirstParticipantId();
            String firstParticipantId = conference.getURI_TO_PID_MAP().get(viewedUri);
            if(firstParticipantId == null){
                log.info("welink not allow to partView");
                throw new MyHttpException(409, GwErrorCode.WELINK_NO_CALID_VIDEO_SOURCE.toString());
            }
//            if (!reuse) {
                String url = "/mmc/control/conferences/partView?conferenceID=" + conferenceId + "&participantID=" + participantId;
                String body = "{\"participantID\":\"" + firstParticipantId + "\",\"viewType\":2}";
                weLinkMeetingManagementService.participantsControl(url, body, conference.getConferenceToken());
                //Todo 通知上级观看对象改变
                if(allocateCasChannelInfo != null){
                    SubPic subPic = multiPicInfo.getSubPicList().get(0);
                    ParticipantInfo participantInfo = conference.getParticipantInfo(viewedUri);
                    subPic.setName(participantInfo.getName());
                    conference.notifyUp(multiPicInfo, conferenceId, conference.getSmcAccessCode(), allocateCasChannelInfo.getCascadeChannelInfo().getIndex());
                    if(allocateCasChannelInfo.getCascadeChannelInfo().getIndex() == 0){
                        conference.sendMasterVideoSourceNotify(viewedUri);
                    }
                }
//            }
        }
        return jsonObject;
    }


    @Override
    public void reNameMainChannel(CascadeChannelNotifyInfo cascadeChannelNotifyInfo) throws MyHttpException {
        String confCasId = cascadeChannelNotifyInfo.getConfCasId();
        WelinkConference conference = welinkMeetingManagerService.getWelinkConferenceByAccessCode(confCasId);
        if(conference == null){
            return;
        }
        String confId = conference.getId();
        CascadeChannelInfo cascadeChannelInfo = welinkCasChannelManageService.getCascadeChannelInfoByIndex(confId, cascadeChannelNotifyInfo.getIndex());
        if(cascadeChannelInfo == null || cascadeChannelInfo.getIndex() != 0){
            return;
        }
        if (cascadeChannelInfo.isMain()) {
            String broadcastUri = "";
            if(cascadeChannelNotifyInfo.getMultiPicInfo().getPicNum() != null && cascadeChannelNotifyInfo.getMultiPicInfo().getPicNum()>1){
                broadcastUri = "00000000-0000-0000-0000-000000000000";
            }else{
                broadcastUri = cascadeChannelNotifyInfo.getMultiPicInfo().getFirstParticipantId();
            }
            conference.sendAllParticipantsBroadcast(broadcastUri, cascadeChannelInfo.getParticipantId());
            conference.setMainChannelSource(broadcastUri);
        }
        ParticipantInfo participantInfo = conference.getParticipantInfo(cascadeChannelInfo.getParticipantId());
        if(participantInfo == null){
            return;
        }
        MultiPicInfo multiPicInfo = cascadeChannelNotifyInfo.getMultiPicInfo();
        if(multiPicInfo == null || (StringUtils.isEmpty(multiPicInfo.getFirstParticipantId()) && (multiPicInfo.getPicNum() == null || multiPicInfo.getPicNum() == 1))){
            return;
        }


        RenameNamParticipantsRequest renameNamParticipantsRequest = new RenameNamParticipantsRequest();
        renameNamParticipantsRequest.setNumber(participantInfo.getUri());
        //Todo 上级需要传名称
        String newName = "多画面";
        if(multiPicInfo.getSubPicList().size() == 1){
            newName = multiPicInfo.getSubPicList().get(0).getName();
            if(newName == null){
                newName = multiPicInfo.getFirstParticipantId();
            }
        }
        renameNamParticipantsRequest.setNewName(newName);
        String participantId = conference.getURI_TO_PID_MAP().get(participantInfo.getUri());
        if(participantId != null){
            renameNamParticipantsRequest.setParticipantID(participantId);
        }
        weLinkMeetingControlService.renameNamParticipants(confId, renameNamParticipantsRequest, conference.getConferenceToken());
    }

    @Override
    public AddCasChannelResp addCasChannel(AddCasChannelReq addCasChannelReq, String token) throws MyHttpException{
        WelinkNodeData welinkNodeData = welinkMeetingManagerService.getWelinkNodeData();
        String accessCode = welinkNodeData.getAreaCode() + addCasChannelReq.getConfId();
        String welinkNodeName = welinkNodeData.getName();
        welinkMeetingManagerService.createWelinkConference(addCasChannelReq.getConfId(),addCasChannelReq.getUpAccessCode(),welinkNodeName, accessCode,addCasChannelReq.getCascadeNum());
        WelinkConference conference = welinkMeetingManagerService.getWelinkConference(addCasChannelReq.getConfId());
        AddCasChannelResp addCasChannelResp = new AddCasChannelResp();
        if (conference == null) {
            return addCasChannelResp;
        }
        conference.setRemoteGwId(addCasChannelReq.getGwId());
        //添加welink级联通道
        List<Attendee> attendees = new ArrayList<>();
        for (int i = 0; i < addCasChannelReq.getCascadeNum(); i++) {
            String callerNumber = getWelinkUriByNumber(addCasChannelReq.getUpAccessCode(), i, addCasChannelReq.getCascadeNum());
            Attendee attend = new Attendee(callerNumber, "welink" + (i + 1));
            attend.setCascadeChannel(i != 0);
            attend.setIsAutoInvite(0);
            attend.setUri(callerNumber);
            attendees.add(attend);
        }
        AddParticipantsRequest addParticipantsRequest = new AddParticipantsRequest();
        addParticipantsRequest.setAttendees(attendees);
        String confToken = weLinkWebSocketService.getToken(addCasChannelReq.getConfId());
        weLinkMeetingControlService.addParticipants(addCasChannelReq.getConfId(),addParticipantsRequest, confToken);

        addCasChannelResp.setRemoteAccessCode(accessCode);
        addCasChannelResp.setRemoteNodeName(welinkNodeName);
        addCasChannelResp.setRemoteSmcVersionType(SmcVersionType.V3);
        addCasChannelResp.setGwId(welinkNodeData.getGwId());
        String[] resultArray = weLinkWebSocketService.getConferencePwd(addCasChannelReq.getConfId());
        String password = null;
        if (resultArray != null && resultArray.length > 0) {
            password = resultArray[0];
        }
        addCasChannelResp.setChairPassword(password);
        weLinkWebSocketService.connect(addCasChannelReq.getConfId());
        return addCasChannelResp;
    }

    @Override
    public JSONObject getOne(String conferenceId, String token) {
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        JSONObject resp = new JSONObject();
        JSONObject conference = new JSONObject();
        if (welinkConference == null) {
            return resp;
        }
        conference.put("id", welinkConference.getId());
        JSONObject multiConferenceService = new JSONObject();
        multiConferenceService.put("accessCode", welinkConference.getId());
        resp.put("multiConferenceService", multiConferenceService);
        conference.put("accessCode", welinkConference.getId());
        GetMeetingDetailRequest getMeetingDetailRequest = new GetMeetingDetailRequest();
        getMeetingDetailRequest.setConferenceID(conferenceId);
        getMeetingDetailRequest.setLimit(500);
        try {
            com.suntek.vdm.gw.welink.api.response.GetMeetingDetailResponse getMeetingDetailResponse = weLinkMeetingManagementService.getMeetingDetail(getMeetingDetailRequest, token);
            String subject = getMeetingDetailResponse.getConferenceData().getSubject();
            conference.put("subject", subject);
            List<PasswordEntry> passwordEntrys = getMeetingDetailResponse.getConferenceData().getPasswordEntry();
            for(PasswordEntry passwordEntry: passwordEntrys){
                if("chair".equals(passwordEntry.getConferenceRole())){
                    conference.put("chairmanPassword", passwordEntry.getPassword());
                }else{
                    conference.put("guestPassword", passwordEntry.getPassword());
                }
            }
        } catch (MyHttpException exception) {
            exception.printStackTrace();
        }
        resp.put("conference", conference);
        return resp;
    }

    @Override
    public void deleteMeeting(String conferenceId, String token) throws MyHttpException {
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (welinkConference == null) {
            return;
        }
        weLinkMeetingManagementService.deleteMeeting(conferenceId, token);
        welinkMeetingManagerService.delMeeting(conferenceId);
    }

    @Override
    public CasConferenceInfosResponse getCasConferenceInfos(String conferenceId) {
        CasConferenceInfosResponse response = new CasConferenceInfosResponse();
        response.setCode(0);
        CasConfInfo casConfInfo = new CasConfInfo();
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (welinkConference == null) {
            return null;
        }
        casConfInfo.setName(welinkConference.getName());
        casConfInfo.setConfCasId(welinkConference.getAccessCode());
        casConfInfo.setIsWeLink(true);
        response.setData(casConfInfo);
        return response;
    }

    @Override
    public void addParticipants(List<JSONObject> participantsList, String conferenceId,String token,String query) throws MyHttpException{
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (welinkConference == null) {
            return;
        }
        AddParticipantsRequest request = new AddParticipantsRequest();
        List<Attendee> attendees = new ArrayList<>();
        for (JSONObject item : participantsList) {
            ParticipantReq participant = item.toJavaObject(ParticipantReq.class);
            Attendee attendee = new Attendee(participant.getUri(), participant.getName());
            attendees.add(attendee);
        }
        request.setAttendees(attendees);
        try {
            String conferenceToken = welinkConference.getConferenceToken();
            weLinkMeetingControlService.addParticipants(conferenceId,request, conferenceToken);
            log.info("add participant success");
        } catch (Exception e) {
            log.info("add participant error:{}", e.getMessage());
        }
    }

    @Override
    public void delAttendees(List<String> participantsList, String conferenceId) throws MyHttpException {
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (welinkConference == null) {
            return;
        }
        Map<String, ParticipantInfo> allParticipantMap = welinkConference.getAllParticipantMap();
        DelAttendeesRequest delAttendeesRequest = new DelAttendeesRequest();
        List<DelAttendInfo> delAttendInfoList = new ArrayList<>();
        Map<String, String> uriToPid = welinkConference.getURI_TO_PID_MAP();
        for (String uri : participantsList) {
            if (allParticipantMap.containsKey(uri)) {
                DelAttendInfo delAttendInfo = new DelAttendInfo();
                ParticipantInfo participantInfo = allParticipantMap.get(uri);
                String number = participantInfo.getPhone() == null ? participantInfo.getUserUUID() : participantInfo.getPhone();
                delAttendInfo.setParticipantID(uriToPid.get(uri));
                delAttendInfo.setNumber(number);
                delAttendInfoList.add(delAttendInfo);
                Map<String, ParticipantInfo> pIdToRepeatMap = participantInfo.getPIdToRepeatMap();
                if(pIdToRepeatMap.isEmpty()){
                    continue;
                }
                for(ParticipantInfo repeateParticipantInfo: pIdToRepeatMap.values()){
                    DelAttendInfo delAttendInfo1 = new DelAttendInfo();
                    String repeateNumber = repeateParticipantInfo.getPhone() == null ? repeateParticipantInfo.getUserUUID() : repeateParticipantInfo.getPhone();
                    delAttendInfo1.setParticipantID(repeateParticipantInfo.getPId());
                    delAttendInfo1.setNumber(repeateNumber);
                    delAttendInfoList.add(delAttendInfo1);
                }
            }
        }
        delAttendeesRequest.setBulkDelAttendInfo(delAttendInfoList);
        weLinkMeetingControlService.delAttendees(conferenceId, delAttendeesRequest, welinkConference.getConferenceToken());
    }

    @Override
    public void meetingControl(String conferenceId, MeetingControlRequestEx requestEx) throws MyHttpException {
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (welinkConference == null) {
            return;
        }
        Map<String, String> uriToPid = welinkConference.getURI_TO_PID_MAP();
        if (requestEx.getBroadcaster() != null) {
            //广播会场
            String uri = requestEx.getBroadcaster();
            CasChannelParameter watchCasChannel = CasChannelParameter.valueOf(uri);
            boolean isLockMaster = false;
            if(watchCasChannel != null){
                AllocateCasChannelInfo mainCasChannel = welinkCasChannelManageService.allocateMainCasChannel(conferenceId);
                if(mainCasChannel == null){
                    throw new MyHttpException(409, GwErrorCode.CASCADE_CHANNEL_ALLOCATION_FAILED.toString());
                }
                uri = mainCasChannel.getCascadeChannelInfo().getParticipantId();
//                锁住主通道
                String participantId = uriToPid.get(uri);
                if(participantId != null) {
                    LockViewParticipantsRequest lockViewParticipantsRequest = new LockViewParticipantsRequest(1);
                    weLinkMeetingControlService.lockViewParticipants(conferenceId, participantId, lockViewParticipantsRequest, welinkConference.getConferenceToken());
                    isLockMaster = true;
                }
            }
            String participantId = uriToPid.get(uri);
            if(participantId != null) {
                if(requestEx.getIsLock()!=null&&requestEx.getIsLock()){
                    LockViewParticipantsRequest lockViewParticipantsRequest = new LockViewParticipantsRequest(1);
                    weLinkMeetingControlService.lockViewParticipants(conferenceId, participantId, lockViewParticipantsRequest, welinkConference.getConferenceToken());
                }
                weLinkMeetingControlService.broadcastParticipants(conferenceId, participantId, welinkConference.getConferenceToken());
                if(isLockMaster){
                    LockViewParticipantsRequest lockViewParticipantsRequest = new LockViewParticipantsRequest(0);
                    weLinkMeetingControlService.lockViewParticipants(conferenceId, participantId, lockViewParticipantsRequest, welinkConference.getConferenceToken());
                }
                if(requestEx.getIsLock()!=null&&requestEx.getIsLock()){
                    LockViewParticipantsRequest lockViewParticipantsRequest = new LockViewParticipantsRequest(0);
                    weLinkMeetingControlService.lockViewParticipants(conferenceId, participantId, lockViewParticipantsRequest, welinkConference.getConferenceToken());
                }
                welinkConference.sendUpchannelSourceChange(uri);
                welinkConference.sendMasterVideoSourceNotify(uri);
            }else {
                //取消广播
                welinkConference.sendAllParticipantsBroadcast("", null);
                welinkConference.setBroadcastUri("");
            }
        }
        Boolean isMute = requestEx.getIsMute();
        if (isMute != null) {
            //全场静音(解除静音)
            weLinkMeetingControlService.setMuteAll(conferenceId, new SetMuteRequest(isMute ? 1 : 0,0), welinkConference.getConferenceToken());
            if (isMute) {
                //如果是全场静音，需要再发送完请求之后取消静音主级联通道
                ParticipantInfo masterParticipant = welinkConference.getMasterParticipant();
                if (masterParticipant == null) {
                    return;
                }
                String uri = uriToPid.get(masterParticipant.getUri());
                weLinkMeetingControlService.setMute(conferenceId, uri, new SetMuteRequest(0, null), welinkConference.getConferenceToken());
            }
        }
        if (requestEx.getIsOnline() != null) {
            //呼叫全场
            if(requestEx.getIsOnline()){
                Map<String, ParticipantInfo> allParticipantMap = welinkConference.getAllParticipantMap();
                AddParticipantsRequest addParticipantsRequest = null;
                for(ParticipantInfo participantInfo : allParticipantMap.values()){
                    String participantId = uriToPid.get(participantInfo.getUri());
                    if(!StringUtils.isEmpty(participantId)){
                        continue;
                    }
                    if(addParticipantsRequest == null){
                        addParticipantsRequest = new AddParticipantsRequest();
                        addParticipantsRequest.setAttendees(new ArrayList<Attendee>());
                    }
                    addParticipantsRequest.getAttendees().add(new Attendee(participantInfo));
                }
                if(addParticipantsRequest != null){
                    weLinkMeetingControlService.addParticipants(conferenceId,addParticipantsRequest,welinkConference.getConferenceToken());
                }
            }
        }

        if(requestEx.getSpokesman() != null){
            String uri = requestEx.getSpokesman();
            CasChannelParameter watchCasChannel = CasChannelParameter.valueOf(uri);
            boolean isLockMaster = false;
            if(watchCasChannel != null){
                AllocateCasChannelInfo mainCasChannel = welinkCasChannelManageService.allocateMainCasChannel(conferenceId);
                if(mainCasChannel == null){
                    throw new MyHttpException(409, GwErrorCode.CASCADE_CHANNEL_ALLOCATION_FAILED.toString());
                }
                uri = mainCasChannel.getCascadeChannelInfo().getParticipantId();
                String participantId = uriToPid.get(uri);
                if(participantId != null) {
                    LockViewParticipantsRequest lockViewParticipantsRequest = new LockViewParticipantsRequest(1);
                    weLinkMeetingControlService.lockViewParticipants(conferenceId, participantId, lockViewParticipantsRequest, welinkConference.getConferenceToken());
                    isLockMaster = true;
                }
            }
            String participantId = uriToPid.get(uri);
            if(participantId != null) {
                weLinkMeetingControlService.rollCallParticipants(conferenceId, participantId, welinkConference.getConferenceToken());
                if(isLockMaster){
                    LockViewParticipantsRequest lockViewParticipantsRequest = new LockViewParticipantsRequest(0);
                    weLinkMeetingControlService.lockViewParticipants(conferenceId, participantId, lockViewParticipantsRequest, welinkConference.getConferenceToken());
                }
            }
        }
        if(requestEx.getIsVoiceActive() != null){

            //分配主通道
            AllocateCasChannelInfo mainCasChannel = welinkCasChannelManageService.allocateMainCasChannel(conferenceId);
            if(mainCasChannel == null){
                throw new MyHttpException(409, GwErrorCode.CASCADE_CHANNEL_ALLOCATION_FAILED.toString());
            }
            String masterPid = uriToPid.get(mainCasChannel.getCascadeChannelInfo().getParticipantId());
            if(requestEx.getIsVoiceActive()){
                String sitePid = "aa";
//                Map<String, ParticipantInfo> allParticipantMap = welinkConference.getAllParticipantMap();
//                for(ParticipantInfo participantInfo: allParticipantMap.values()){
//                    if(!welinkConference.isUploadToSmc(welinkConference.getSmcAccessCode(),participantInfo.getUri()) && welinkConference.getONLINE_URI_MAP().get(participantInfo.getUri())){
//                        sitePid = uriToPid.get(participantInfo.getUri());
//                        break;
//                    }
//                }
//                if("".equals(sitePid)){
//                    sitePid = masterPid;
//                }
                RestMixedPictureBody displayMultiPictureRequest = new RestMixedPictureBody(1, "Single", sitePid, 20);
                weLinkMeetingControlService.displayMultiPicture(conferenceId, displayMultiPictureRequest, welinkConference.getConferenceToken());
//                LockViewParticipantsRequest lockViewParticipantsRequest = new LockViewParticipantsRequest(0);
//                weLinkMeetingControlService.lockViewParticipants(conferenceId, masterPid, lockViewParticipantsRequest, welinkConference.getConferenceToken());
                RestSwitchModeReqBody restSwitchModeReqBody = new RestSwitchModeReqBody("VAS", 0);
                weLinkMeetingControlService.displayMode(conferenceId, restSwitchModeReqBody, welinkConference.getConferenceToken());
            }else {
//                LockViewParticipantsRequest lockViewParticipantsRequest = new LockViewParticipantsRequest(1);
//                weLinkMeetingControlService.lockViewParticipants(conferenceId, masterPid, lockViewParticipantsRequest, welinkConference.getConferenceToken());
//                RestSwitchModeReqBody restSwitchModeReqBody = new RestSwitchModeReqBody("Fixed", 0);
//                weLinkMeetingControlService.displayMode(conferenceId, restSwitchModeReqBody, welinkConference.getConferenceToken());
                weLinkMeetingControlService.broadcastParticipants(conferenceId, masterPid, welinkConference.getConferenceToken());
            }
        }
    }

    @Override
    public void durationMeeting(String conferenceId, DurationMeetingRequestEx requestEx) throws MyHttpException{
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (welinkConference == null) {
            return;
        }
        DurationMeetingRequest request = new DurationMeetingRequest();
        if (requestEx.getExtendTime() == null) {
            request.setAuto(1);
        } else {
            request.setDuration(requestEx.getExtendTime());
            request.setAuto(0);
        }
        weLinkMeetingControlService.durationMeeting(conferenceId, request, welinkConference.getConferenceToken());
    }

    @Override
    public void stopMeeting(String conferenceId) throws MyHttpException{
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (welinkConference == null) {
            return;
        }
        StopMeetingRequest request = new StopMeetingRequest();
        request.setOperation(0);
        weLinkMeetingControlService.stopMeeting(conferenceId, request, welinkConference.getConferenceToken());
        welinkMeetingManagerService.delMeeting(conferenceId);
    }

    @Override
    public void broadcastParticipants(String conferenceId, String participantId) throws MyHttpException{
        WelinkConference welinkConference = welinkMeetingManagerService.getWelinkConference(conferenceId);
        if (welinkConference == null) {
            return;
        }
        Map<String, String> uriToPid = welinkConference.getURI_TO_PID_MAP();
        String s = weLinkMeetingControlService.broadcastParticipants(conferenceId, uriToPid.get(participantId), welinkConference.getConferenceToken());
        System.out.println(s);
    }


}

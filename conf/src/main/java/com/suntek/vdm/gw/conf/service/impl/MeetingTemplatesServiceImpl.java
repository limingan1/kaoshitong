package com.suntek.vdm.gw.conf.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.conf.api.request.AddTemplatesRequestEx;
import com.suntek.vdm.gw.conf.api.request.ChildNodeInfos;
import com.suntek.vdm.gw.conf.api.request.ModifyTemplatesRequestEx;
import com.suntek.vdm.gw.conf.api.request.ScheduleMeetingRequestEx;
import com.suntek.vdm.gw.conf.api.response.AddTemplatesResponseEx;
import com.suntek.vdm.gw.conf.api.response.GetTemplatesResponseEx;
import com.suntek.vdm.gw.conf.api.response.ModifyTemplatesResponseEx;
import com.suntek.vdm.gw.conf.pojo.MeetingInfo;
import com.suntek.vdm.gw.conf.service.MeetingInfoManagerService;
import com.suntek.vdm.gw.conf.service.MeetingManagerService;
import com.suntek.vdm.gw.conf.service.MeetingTemplatesService;
import com.suntek.vdm.gw.core.entity.MeetingTemplateData;
import com.suntek.vdm.gw.core.service.MeetingTemplateDataService;
import com.suntek.vdm.gw.common.util.CommonHelper;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.smc.pojo.*;
import com.suntek.vdm.gw.smc.request.meeting.management.ScheduleMeetingRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.AddTemplatesRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.GetTemplatesListRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.ModifyTemplatesRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.ScheduleByTemplatesRequest;
import com.suntek.vdm.gw.smc.response.meeting.management.ScheduleMeetingResponse;
import com.suntek.vdm.gw.smc.response.meeting.templates.*;
import com.suntek.vdm.gw.smc.service.SmcMeetingTemplatesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class MeetingTemplatesServiceImpl extends BaseServiceImpl implements MeetingTemplatesService {
    @Autowired
    private MeetingTemplateDataService meetingTemplateDataService;
    @Autowired
    private SmcMeetingTemplatesService smcMeetingTemplatesService;
    @Autowired
    private MeetingManagerService meetingManagerService;
    @Autowired
    private MeetingInfoManagerService meetingInfoManagerService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private NodeManageService nodeManageService;


    public AddTemplatesResponseEx addTemplates(AddTemplatesRequestEx request, String token) throws MyHttpException {
        boolean hasChild = false;
        int type;
        List<ChildNodeInfos> child = request.getChild();
        if (child != null && child.size() > 0) {
            setChildAttendeesId(child);
            hasChild = true;
            type = 2;
        } else {
            //Fixme 此处逻辑需要优化  和之前的不一样
            type = 1;
        }
        int cascadeNum = 1;
        if (request.getCascadeNum() != null) {
            cascadeNum = request.getCascadeNum();
        }
        AddTemplatesRequest addTemplatesRequest = new AddTemplatesRequest();
        BeanUtils.copyProperties(request, addTemplatesRequest);
        AddTemplatesResponse smcResponse = smcMeetingTemplatesService.addTemplates(addTemplatesRequest, getSmcToken(token));
        //保存到数据库
        if (hasChild) {
            MeetingTemplateData meetingTemplateData = new MeetingTemplateData();
            meetingTemplateData.setTemplateId(smcResponse.getId());
            meetingTemplateData.setChild(JSON.toJSONString(child));
            meetingTemplateData.setCascadeNum(cascadeNum);
            meetingTemplateData.setType(type);
            meetingTemplateDataService.add(meetingTemplateData);
        }
        AddTemplatesResponseEx response = new AddTemplatesResponseEx();
        BeanUtils.copyProperties(smcResponse, response);
        response.setCascadeNum(cascadeNum);
        response.setChild(child);
        return response;
    }

    public void setChildAttendeesId(List<ChildNodeInfos> child) {
        if (child == null || child.isEmpty()) {
            return;
        }
        for (ChildNodeInfos childNodeInfos : child) {
            List<AttendeeReq> attendees = childNodeInfos.getAttendees();
            if (attendees != null && !attendees.isEmpty()) {
                for (AttendeeReq attendee : attendees) {
                    if (attendee.getId() == null || "".equals(attendee.getId())) {
                        attendee.setId(UUID.randomUUID().toString());
                    }
                }
            }
            setChildAttendeesId(childNodeInfos.getChild());
        }
    }

    @Override
    public ModifyTemplatesResponseEx modifyTemplates(String templateId, ModifyTemplatesRequestEx request, String token) throws MyHttpException {
        boolean hasChild = false;
        int type;
        List<ChildNodeInfos> child = request.getChild();
        if (child != null && child.size() > 0) {
            setChildAttendeesId(child);
            hasChild = true;
            type = 2;
        } else {
            //Fixme 此处逻辑需要优化  和之前的不一样
            type = 1;
        }
        int cascadeNum = 1;
        if (request.getCascadeNum() != null) {
            cascadeNum = request.getCascadeNum();
        }
        ModifyTemplatesRequest modifyTemplatesRequest = new ModifyTemplatesRequest();
        BeanUtils.copyProperties(request, modifyTemplatesRequest);
        ModifyTemplatesResponse smcResponse = smcMeetingTemplatesService.modifyTemplates(templateId, modifyTemplatesRequest, getSmcToken(token));
        //数据库修改模板
        MeetingTemplateData old = meetingTemplateDataService.getOneByTemplateId(templateId);
        if (hasChild) {
            if (old == null) {
                old = new MeetingTemplateData();
                old.setTemplateId(templateId);
                old.setChild(JSON.toJSONString(child));
                old.setCascadeNum(cascadeNum);
                old.setType(type);
                meetingTemplateDataService.add(old);
            } else {
                old.setChild(JSON.toJSONString(child));
                old.setCascadeNum(cascadeNum);
                old.setType(type);
                meetingTemplateDataService.update(old);
            }
        } else {
            if (old != null) {
                meetingTemplateDataService.del(old.getId());
            }
        }
        ModifyTemplatesResponseEx response = new ModifyTemplatesResponseEx();
//        BeanUtils.copyProperties(smcResponse, response);
        response.setCascadeNum(cascadeNum);
        response.setChild(child);
        return response;
    }

    @Override
    public GetTemplatesResponseEx getTemplates(String templateId, String token) throws MyHttpException {
        GetTemplatesResponse smcResponse = smcMeetingTemplatesService.getTemplates(templateId, getSmcToken(token));
        GetTemplatesResponseEx response = new GetTemplatesResponseEx();
        BeanUtils.copyProperties(smcResponse, response);
        MeetingTemplateData old = meetingTemplateDataService.getOneByTemplateId(templateId);
        if (old != null) {
            response.setCascadeNum(old.getCascadeNum());
            List<ChildNodeInfos> child = JSON.parseObject(old.getChild(), new TypeReference<List<ChildNodeInfos>>() {
            });
            response.setChild(child);
            response.setType(old.getType());
        }
        return response;
    }

    @Override
    public GetTemplatesLisResponse getTemplatesList(int page, int size, String sort, GetTemplatesListRequest request, String token) throws MyHttpException {
        return smcMeetingTemplatesService.getTemplatesList(page, size, sort, request, getSmcToken(token));
    }

    @Override
    public GetTemplatesParticipantsResponse getTemplatesParticipants(String templateId, int page, int size, String name, String sort, String token) throws MyHttpException {
        return smcMeetingTemplatesService.getTemplatesParticipants(templateId, page, size, name, sort, getSmcToken(token));
    }

    @Override
    public ScheduleByTemplatesResponse scheduleByTemplates(String templateId, ScheduleByTemplatesRequest request, String token) throws MyHttpException {
        String subject = request.getSubject();
        request.setSubject(nodeDataService.getLocal().getName() + subject);
        String tempStartTime = null;
        boolean updateTimeSign = false;
        if (SystemConfiguration.smcVersionIsV2() && "INSTANT_CONFERENCE".equals(request.getConferenceTimeType())) {
            tempStartTime = request.getScheduleStartTime();
            request.setScheduleStartTime("1970-01-01 00:00:00 UTC");
            updateTimeSign = true;
        }
        ScheduleByTemplatesResponse smcResponse = smcMeetingTemplatesService.scheduleByTemplates(templateId, request, getSmcToken(token));
        //转换响应
        ScheduleMeetingResponse scheduleMeetingResponse = CommonHelper.copyBean(smcResponse, ScheduleMeetingResponse.class);
        //转换请求
        GetTemplatesResponse getTemplatesResponse = smcMeetingTemplatesService.getTemplates(templateId, getSmcToken(token));
        //简化请求
        ScheduleMeetingRequest scheduleMeetingRequest = new ScheduleMeetingRequest();
        ConferenceReq conferenceRsp = CommonHelper.copyBean(smcResponse.getConference(), ConferenceReq.class);
        conferenceRsp.setConferenceTimeType(request.getConferenceTimeType());
        conferenceRsp.setScheduleStartTime(updateTimeSign ? tempStartTime : request.getScheduleStartTime());//不修改召集子会议的请求
        conferenceRsp.setDuration(request.getDuration());
        scheduleMeetingRequest.setConference(conferenceRsp);
        MultiConferenceServiceReq multiConferenceServiceReq = new MultiConferenceServiceReq();
        ConferenceCapabilityReq conferenceCapabilityReq = CommonHelper.copyBean(getTemplatesResponse.getConferenceCapabilitySetting(), ConferenceCapabilityReq.class);
        ConferencePolicyReq conferencePolicyReq = CommonHelper.copyBean(getTemplatesResponse.getConferencePolicySetting(), ConferencePolicyReq.class);
        multiConferenceServiceReq.setConferenceCapabilitySetting(conferenceCapabilityReq);
        multiConferenceServiceReq.setConferencePolicySetting(conferencePolicyReq);
        scheduleMeetingRequest.setMultiConferenceService(multiConferenceServiceReq);
        StreamServiceReq streamServiceReq = CommonHelper.copyBean(getTemplatesResponse.getStreamService(), StreamServiceReq.class);
        scheduleMeetingRequest.setStreamService(streamServiceReq);
        ScheduleMeetingRequestEx scheduleMeetingRequestEx = CommonHelper.copyBean(scheduleMeetingRequest, ScheduleMeetingRequestEx.class);
        //增加下级的参数
        MeetingTemplateData old = meetingTemplateDataService.getOneByTemplateId(templateId);
        if (old != null) {
            scheduleMeetingRequestEx.setCascadeNum(old.getCascadeNum());
            List<ChildNodeInfos> child = JSON.parseObject(old.getChild(), new TypeReference<List<ChildNodeInfos>>() {
            });
            //处理casOrgId，防止因为远端节点Id变化而导致召集会议失败
            checkNodeChange(old, child);

            scheduleMeetingRequestEx.setChild(child);
        }
        //smc2.0召集会议不返回会议名称
        scheduleMeetingRequestEx.getConference().setSubject(subject);

        meetingManagerService.scheduleConferences(scheduleMeetingRequestEx, scheduleMeetingResponse, token);
        meetingManagerService.transferScheduleMeetingData(smcResponse.getConference(), smcResponse.getConference());
        return smcResponse;
    }

    private void checkNodeChange(MeetingTemplateData old, List<ChildNodeInfos> child){
        GwNode gwNode = nodeManageService.getOrganizationNode();
        boolean isChangge = false;
        for(ChildNodeInfos childNodeInfos: child){
            String casOrgId = childNodeInfos.getCasOrgId();
            GwId gwId = GwId.valueOf(casOrgId);
            if(isChangeWithOrgTree(gwId, gwNode)){
                childNodeInfos.setCasOrgId(gwId.toString());
                isChangge = true;
            }
        }
        if(isChangge){
            old.setChild(JSON.toJSONString(child));
            meetingTemplateDataService.update(old);
        }
    }
    private Boolean isChangeWithOrgTree(GwId gwId, GwNode gwNode){
        if(gwId.getNodeId().equals(gwNode.getId())){
            if(!gwId.getAreaCode().equals(gwNode.getAreaCode())){
                gwId.setAreaCode(gwNode.getAreaCode());
                return true;
            }
            return false;
        }
        if(gwId.getAreaCode().equals(gwNode.getAreaCode())){
            gwId.setNodeId(gwNode.getId());
            return true;
        }
        if(gwNode.getChild() == null || gwNode.getChild().isEmpty()){
            return false;
        }
        for(GwNode childGwNode: gwNode.getChild()){
            if(isChangeWithOrgTree(gwId, childGwNode)){
                return true;
            }
        }
        return false;
    }


    @Override
    public void delTemplates(String templateId, String token) throws MyHttpException {
        smcMeetingTemplatesService.delTemplates(templateId, getSmcToken(token));
        MeetingTemplateData old = meetingTemplateDataService.getOneByTemplateId(templateId);
        if (old != null) {
            meetingTemplateDataService.del(old.getId());
        }
    }

    @Override
    public GetAttendeesByIdResponse getAttendeesById(String templateId, int page, int size, String token) throws MyHttpException {
        return smcMeetingTemplatesService.getAttendeesById(templateId, page, size, getSmcToken(token));
    }

    @Override
    public ConferencesToTemplateResponse conferencesToTemplate(String conferencesId, String token) throws MyHttpException {
        ConferencesToTemplateResponse smcResponse = smcMeetingTemplatesService.conferencesToTemplate(conferencesId, getSmcToken(token));
        MeetingInfo meetingInfo = meetingInfoManagerService.get(conferencesId);
        //如果存在下级会议
        if (meetingInfo.getChildMeetingInfoMap().size() != 0) {
            MeetingTemplateData meetingTemplateData = new MeetingTemplateData();
            meetingTemplateData.setTemplateId(smcResponse.getId());
//            vdmMeetingTemplate.setChild(JSON.toJSONString(request.getChild()));
//            vdmMeetingTemplate.setCascadeNum(meetingInfo.get);
            meetingTemplateData.setType(2);
            meetingTemplateDataService.add(meetingTemplateData);
        }
        return smcResponse;
    }
}

package com.suntek.vdm.gw.smc.service.impl;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingTemplateasService;
import com.suntek.vdm.gw.smc.request.meeting.templates.AddTemplatesRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.GetTemplatesListRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.ModifyTemplatesRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.ScheduleByTemplatesRequest;
import com.suntek.vdm.gw.smc.response.meeting.templates.*;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.smc.service.SmcMeetingTemplatesService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmcMeetingTemplatesServiceImpl extends SmcBaseServiceImpl implements SmcMeetingTemplatesService {
    @Value("${useAdapt}")
    private Boolean useAdapt;

    @Autowired
    AdaptMeetingTemplateasService adaptMeetingTemplateasService;

    @Autowired
    @Qualifier("smcHttpServiceImpl")
    private HttpService httpService;

    /**
     * 添加会议模板
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public AddTemplatesResponse addTemplates(AddTemplatesRequest request, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingTemplateasService.addTemplates(request, token);
        }else {
            response = httpService.post("/conferences/templates", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, AddTemplatesResponse.class);
    }

    /**
     * 修改会议模板
     * @param templateId
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public ModifyTemplatesResponse modifyTemplates(String templateId, ModifyTemplatesRequest request, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingTemplateasService.modifyTemplates(templateId, request, token);
        }else {
            response = httpService.put("/conferences/templates/"+templateId, request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, ModifyTemplatesResponse.class);
    }

    /**
     * 获取单个会议模板
     * @param templateId
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public GetTemplatesResponse getTemplates(String templateId, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingTemplateasService.getTemplates(templateId, token);
        }else {
            response = httpService.get("/conferences/templates/"+templateId, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetTemplatesResponse.class);
    }

    /**
     * 查询会议模板列表
     * @param page
     * @param size
     * @param sort
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public GetTemplatesLisResponse getTemplatesList(int page, int size, String sort, GetTemplatesListRequest request, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingTemplateasService.getTemplatesList(page, size, sort, request, token);
        }else {
            String url="/conferences/templates/conditions?page="+page+"&size="+size;
            if (StringUtils.isNotBlank(sort)){
                url=url+"&sort="+sort;
            }
            response = httpService.post(url, request, tokenHandle(token)).getBody();
        }

        return JSON.parseObject(response, GetTemplatesLisResponse.class);
    }

    /**
     *  查看模板会场列表
     * @param templateId
     * @param page
     * @param size
     * @param name
     * @param sort
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public GetTemplatesParticipantsResponse getTemplatesParticipants(String templateId, int page, int size, String name, String sort, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingTemplateasService.getTemplatesParticipants(templateId, page, size, name, sort, token);
        }else {
            String url="/conferences/templates/"+templateId+"/participants?page="+page+"&size="+size;
            if (StringUtils.isNotBlank(sort)){
                url=url+"&sort="+sort;
            }
            if (StringUtils.isNotBlank(name)){
                url=url+"&name="+name;
            }
            response = httpService.get(url, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetTemplatesParticipantsResponse.class);
    }

    /**
     * 使用模板召开会议
     * @param templateId
     * @param request
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public ScheduleByTemplatesResponse scheduleByTemplates(String templateId, ScheduleByTemplatesRequest request, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingTemplateasService.scheduleByTemplates(templateId, request, token);
        }else {
            response = httpService.post("/conferences/templates/"+templateId+"/conference", request, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, ScheduleByTemplatesResponse.class);
    }

    /**
     * 删除会议模板
     * @param templateId
     * @param token
     * @throws MyHttpException
     */
    @Override
    public void delTemplates(String templateId, String token)  throws MyHttpException {
        if(useAdapt){
            adaptMeetingTemplateasService.delTemplates(templateId, token);
        }else {
            httpService.delete("/conferences/templates/"+templateId, null, tokenHandle(token)).getBody();
        }
    }

    /**
     * 根据 id 查找与会者
     * @param templateId
     * @param page
     * @param size
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public GetAttendeesByIdResponse getAttendeesById(String templateId, int page, int size, String token)  throws MyHttpException {
        String response = null;
        if(useAdapt){
            response = adaptMeetingTemplateasService.getAttendeesById(templateId, page, size, token);
        }else {
            response = httpService.get("/conferences/templates/"+templateId+"/attendees?page="+page+"&size="+size, null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, GetAttendeesByIdResponse.class);
    }


    /**
     * 会议转模板
     * @param conferenceId
     * @param token
     * @return
     * @throws MyHttpException
     */
    @Override
    public ConferencesToTemplateResponse  conferencesToTemplate(String conferenceId,String token) throws MyHttpException{
        String response = null;
        if(useAdapt){
            response = adaptMeetingTemplateasService.conferencesToTemplate(conferenceId, token);
        }else {
            response = httpService.post("/conferences/"+conferenceId+"/templates", null, tokenHandle(token)).getBody();
        }
        return JSON.parseObject(response, ConferencesToTemplateResponse.class);
    }

}

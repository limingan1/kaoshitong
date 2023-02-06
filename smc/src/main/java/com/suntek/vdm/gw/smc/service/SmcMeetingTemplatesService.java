package com.suntek.vdm.gw.smc.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.request.meeting.templates.AddTemplatesRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.GetTemplatesListRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.ModifyTemplatesRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.ScheduleByTemplatesRequest;
import com.suntek.vdm.gw.smc.response.meeting.templates.*;

public interface SmcMeetingTemplatesService {
    /**
     * 添加会议模板
     *
     * @param request
     * @param token
     * @return
     * @throws MyHttpException;
     */
    public AddTemplatesResponse addTemplates(AddTemplatesRequest request, String token) throws MyHttpException;

    /**
     * 修改会议模板
     *
     * @param templateId
     * @param request
     * @param token
     * @return
     * @throws MyHttpException;
     */
    public ModifyTemplatesResponse modifyTemplates(String templateId, ModifyTemplatesRequest request, String token) throws MyHttpException;

    /**
     * 获取单个会议模板
     *
     * @param templateId
     * @param token
     * @return
     * @throws MyHttpException;
     */
    public GetTemplatesResponse getTemplates(String templateId, String token) throws MyHttpException;

    /**
     * 查询会议模板列表
     *
     * @param page
     * @param size
     * @param sort
     * @param request
     * @param token
     * @return
     * @throws MyHttpException;
     */
    public GetTemplatesLisResponse getTemplatesList(int page, int size, String sort, GetTemplatesListRequest request, String token) throws MyHttpException;


    /**
     * 查看模板会场列表
     *
     * @param templateId
     * @param page
     * @param size
     * @param name
     * @param sort
     * @param token
     * @return
     * @throws MyHttpException;
     */
    public GetTemplatesParticipantsResponse getTemplatesParticipants(String templateId, int page, int size, String name, String sort, String token) throws MyHttpException;


    /**
     * 使用模板召开会议
     *
     * @param templateId
     * @param request
     * @param token
     * @return
     * @throws MyHttpException;
     */
    public ScheduleByTemplatesResponse scheduleByTemplates(String templateId, ScheduleByTemplatesRequest request, String token) throws MyHttpException;

    /**
     * 删除会议模板
     *
     * @param templateId
     * @param token
     * @throws MyHttpException;
     */
    public void delTemplates(String templateId, String token) throws MyHttpException;

    /**
     * 根据 id 查找与会者
     *
     * @param templateId
     * @param page
     * @param size
     * @param token
     * @return
     * @throws MyHttpException;
     */
    public GetAttendeesByIdResponse getAttendeesById(String templateId, int page, int size, String token) throws MyHttpException;

    /**
     * 会议转模板
     *
     * @param conferencesId
     * @param token
     * @return
     * @throws MyHttpException
     */
    public ConferencesToTemplateResponse conferencesToTemplate(String conferencesId, String token) throws MyHttpException;

}
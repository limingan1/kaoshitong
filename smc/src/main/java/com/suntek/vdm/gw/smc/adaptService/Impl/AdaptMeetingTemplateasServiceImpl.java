package com.suntek.vdm.gw.smc.adaptService.Impl;

import com.alibaba.fastjson.JSON;
import com.huawei.vdmserver.common.dto.ResponseEntityEx;
import com.huawei.vdmserver.common.dto.requestDto.ConfTemplateReq;
import com.huawei.vdmserver.common.dto.requestDto.QueryConfTemplateReq;
import com.huawei.vdmserver.common.dto.requestDto.ScheduledConfByTemplateReq;
import com.huawei.vdmserver.common.vo.ConfVO;
import com.huawei.vdmserver.smc.core.service.SmcConferenceTemplateService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.smc.adaptService.AdaptMeetingTemplateasService;
import com.suntek.vdm.gw.smc.adaptService.util.AdaptHttpStateUtil;
import com.suntek.vdm.gw.smc.request.meeting.templates.AddTemplatesRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.GetTemplatesListRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.ModifyTemplatesRequest;
import com.suntek.vdm.gw.smc.request.meeting.templates.ScheduleByTemplatesRequest;
import com.suntek.vdm.gw.smc.response.meeting.templates.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class AdaptMeetingTemplateasServiceImpl implements AdaptMeetingTemplateasService {
    @Autowired
    @Qualifier("SmcConferenceTemplateService2.0")
    SmcConferenceTemplateService smcConferenceTemplateService;


    @Override
    public String addTemplates(AddTemplatesRequest request, String token) throws MyHttpException {
        ConfTemplateReq confTemplateReq = JSON.parseObject(JSON.toJSONString(request), ConfTemplateReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setData(confTemplateReq);
        ResponseEntityEx<?> object = smcConferenceTemplateService.addConfTemplate(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String modifyTemplates(String templateId, ModifyTemplatesRequest request, String token) throws MyHttpException {
        ConfTemplateReq confTemplateReq = JSON.parseObject(JSON.toJSONString(request), ConfTemplateReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(templateId);
        confVO.setData(confTemplateReq);
        ResponseEntityEx<?> object = smcConferenceTemplateService.editConfTemplate(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getTemplates(String templateId, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setToken(token);
        confVO.setConferenceId(templateId);
        ResponseEntityEx<?> object = smcConferenceTemplateService.queryConfTemplateInfo(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getTemplatesList(int page, int size, String sort, GetTemplatesListRequest request, String token) throws MyHttpException {
        QueryConfTemplateReq queryConfTemplateReq = JSON.parseObject(JSON.toJSONString(request), QueryConfTemplateReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setPage(page);
        confVO.setSize(size);
        confVO.setToken(token);
        confVO.setData(queryConfTemplateReq);
        ResponseEntityEx<?> object = smcConferenceTemplateService.smcConferenceTemplateService(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getTemplatesParticipants(String templateId, int page, int size, String name, String sort, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String scheduleByTemplates(String templateId, ScheduleByTemplatesRequest request, String token) throws MyHttpException {
        ScheduledConfByTemplateReq scheduledConfByTemplateReq = JSON.parseObject(JSON.toJSONString(request), ScheduledConfByTemplateReq.class);
        ConfVO confVO = new ConfVO();
        confVO.setConferenceId(templateId);
        confVO.setToken(token);
        confVO.setData(scheduledConfByTemplateReq);
        ResponseEntityEx<?> object = smcConferenceTemplateService.scheduledConfByTemplate(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String delTemplates(String templateId, String token) throws MyHttpException {
        ConfVO confVO = new ConfVO();
        confVO.setConferenceId(templateId);
        confVO.setToken(token);
        ResponseEntityEx<?> object = smcConferenceTemplateService.delConfTemplate(confVO);
        return AdaptHttpStateUtil.dealAdaptHttpStatus(object);
    }

    @Override
    public String getAttendeesById(String templateId, int page, int size, String token) throws MyHttpException {
        return null;
    }

    @Override
    public String conferencesToTemplate(String conferencesId, String token) throws MyHttpException {
        return null;
    }
}

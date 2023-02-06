package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.core.entity.MeetingTemplateData;

public interface MeetingTemplateDataService extends ServiceFactory<MeetingTemplateData> {
    MeetingTemplateData getOneByTemplateId(String templateId);
}

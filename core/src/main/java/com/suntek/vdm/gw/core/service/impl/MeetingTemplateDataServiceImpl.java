package com.suntek.vdm.gw.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suntek.vdm.gw.core.entity.MeetingTemplateData;
import com.suntek.vdm.gw.core.mapper.MeetingTemplateDataMapper;
import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.core.pojo.TableDate;
import com.suntek.vdm.gw.core.service.MeetingTemplateDataService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MeetingTemplateDataServiceImpl extends ServiceFactoryImpl<MeetingTemplateDataMapper, MeetingTemplateData> implements MeetingTemplateDataService {

    @Override
    public TableDate getPage(Map<String, Object> query, Integer current, Integer size, String order, String orderType) {
        return null;
    }

    @Override
    public BaseState add(MeetingTemplateData info) {
        return state(save(info));
    }

    @Override
    public BaseState update(MeetingTemplateData info) {
        return state(updateById(info));
    }

    @Override
    public MeetingTemplateData getOneByTemplateId(String templateId){
        return    getOneByLambda(new LambdaQueryWrapper<MeetingTemplateData>().eq(MeetingTemplateData::getTemplateId,templateId));
    }
}

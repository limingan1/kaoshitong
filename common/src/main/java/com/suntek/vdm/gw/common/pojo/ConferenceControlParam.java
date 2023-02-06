package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ConferenceControlParam {
    /**
     * 音视频和数据绑定关系
     */
    private List<DataMediaBindRelation> bindRelations;
}
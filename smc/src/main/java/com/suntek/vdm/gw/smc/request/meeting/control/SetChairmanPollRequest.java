package com.suntek.vdm.gw.smc.request.meeting.control;

import com.suntek.vdm.gw.common.pojo.SubPic;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SetChairmanPollRequest {
    /**
     * 间隔时间
     */
    private Integer interval;

    /**
     * 主席轮询模板
     */
    private Map<Integer, List<SubPic>> pollTemplates;

    /**
     * 选择的模板索引
     */
    private Integer templateIndex;

    /**
     * 轮询操作
     */
    private String pollStatus;
}

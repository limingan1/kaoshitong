package com.suntek.vdm.gw.smc.request.meeting.control;

import com.suntek.vdm.gw.smc.pojo.SubPicPollInfo;
import lombok.Data;

import java.util.List;

@Data
public class SetMultiPicPollRequest {
    /**
     * 画面数
     */
    private Integer picNum;

    /**
     * 多画面模式
     */
    private Integer mode;

    /**
     * 轮询画面列表
     */
    private List<SubPicPollInfo> subPicPollInfoList;

    /**
     * 轮询操作
     */
    private String pollStatus;
}

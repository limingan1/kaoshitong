package com.suntek.vdm.gw.smc.request.meeting.control;

import lombok.Data;

import java.util.List;

@Data
public class BatchTextTipsRequest {
    /**
     * 会场Id列表
     */
    private List<String> participantIds;
    /**
     * 内容
     */
    private  SetTextTipsRequest textTip;
}

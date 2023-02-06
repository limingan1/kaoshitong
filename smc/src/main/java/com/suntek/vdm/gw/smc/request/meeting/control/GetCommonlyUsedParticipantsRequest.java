package com.suntek.vdm.gw.smc.request.meeting.control;

import lombok.Data;

import java.util.List;

@Data
public class GetCommonlyUsedParticipantsRequest {
    /**
     *会场名称(>=1字符)（可填）
     */
    private String name;

    /**
     * 是否可做视频源会场(true/false)
     */
    private Boolean videoSource;


    /**
     *发言状态(true/false)（可填）
     */
    private Boolean handUp;

    /**
     *是否可作为视频源会场或语音会场(true/false)（可填）
     */
    private Boolean videoAndVoice;

    /**
     *指定id查询（可填）
     */
    private List<String> participantIds;
}

package com.suntek.vdm.gw.common.pojo.request;

import lombok.Data;

import java.util.List;

@Data
public class GetParticipantsRequest {
    /**
     *会场名称(>=1字符)（可填）
     */
    private String name;

    /**
     *是否可做视频源会场(是：true/否：false)（可填）
     */
    private Boolean videoSource;

    /**
     *是否可作为视频源会场或语音会场(true/false)（可填）
     */
    private Boolean videoAndVoice;

    /**
     *发言状态(true/false)（可填）
     */
    private Boolean handUp;

    /**
     *指定id查询（可填）
     */
    private List<String> participantIds;

    private List<String> towallUris;
}

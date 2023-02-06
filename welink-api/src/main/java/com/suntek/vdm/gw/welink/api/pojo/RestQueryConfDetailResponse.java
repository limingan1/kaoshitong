package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

@Data
public class RestQueryConfDetailResponse {
    /**
     * 会议信息。
     */
    private ConferenceInfo conferenceData;

    /**
     * 与会者列表。
     */
    private PageParticipant data;
}
package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class RestRealTimeInfoResponse {
    /**
     * 所有参加会议的与会者列表，包括未入 会的以及在线的与会者信息。
     */
    private List<RealTimeAttendee> attendees;

    /**
     * 在线会场列表，包括已进入会议、呼叫 中、正在加入会议的与会者列表等。
     */
    private List<RealTimeParticipant> participants;

    /**
     * 会议信息。
     */
    private RealTimeConfInfo confInfo;
}
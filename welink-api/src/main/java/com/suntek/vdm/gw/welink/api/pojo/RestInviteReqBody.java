package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.List;

@Data
public class RestInviteReqBody {
    /**
     * 该与会者列表可以用于 自动邀请。
     */
    private List<Attendee> attendees;
}
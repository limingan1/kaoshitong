package com.suntek.vdm.gw.welink.api.request;

import com.suntek.vdm.gw.welink.api.pojo.Attendee;
import lombok.Data;

import java.util.List;

@Data
public class AddParticipantsRequest extends Attendee {
    private List<Attendee> attendees;
}

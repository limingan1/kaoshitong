package com.suntek.vdm.gw.welink.api.request;

import lombok.Data;

@Data
public class GetMeetingDetailRequest {
    private String conferenceID;
    private String searchKey;
    private Integer offset;
    private Integer limit;
}

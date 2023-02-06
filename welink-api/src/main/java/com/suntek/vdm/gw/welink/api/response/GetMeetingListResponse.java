package com.suntek.vdm.gw.welink.api.response;

import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import com.suntek.vdm.gw.welink.api.pojo.ConferenceInfo;
import com.suntek.vdm.gw.welink.api.pojo.RestListConfResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class GetMeetingListResponse extends RestListConfResponse {

    public GetConditionsMeetingResponse toGetConditionsMeetingResponse(String areaCode) {
        GetConditionsMeetingResponse getConditionsMeetingResponse = new GetConditionsMeetingResponse();
        List<ConferenceInfo> data = this.getData();
        getConditionsMeetingResponse.setEmpty(data.isEmpty());
        getConditionsMeetingResponse.setFirst(true);
        getConditionsMeetingResponse.setLast(true);
        getConditionsMeetingResponse.setTotalPages(1);
        getConditionsMeetingResponse.setTotalElements(data.size());
        List<ScheduleConfBrief> scheduleConfBriefs = new ArrayList<>();
        for (ConferenceInfo conferenceInfo : data) {
            scheduleConfBriefs.add(conferenceInfo.toScheduleConfBrief(areaCode));
        }
        getConditionsMeetingResponse.setContent(scheduleConfBriefs);
        return getConditionsMeetingResponse;
    }
}

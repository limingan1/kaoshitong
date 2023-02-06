package com.suntek.vdm.gw.smc.response.meeting.management;


import lombok.Data;

import java.util.List;

@Data
public class GetMeetingTimeZonesResponse  {
    private List<TimeZones> list;

    @Data
    class TimeZones{
        private  String timeZoneId;
        private  String timeZoneName;
        private  int offset;
        private  String timeZoneDesc;
    }
}

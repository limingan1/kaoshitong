package com.suntek.vdm.gw.welink.api.request;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class GetMeetingListRequest {
    private String searchkey;
    private Integer offset;
    private Integer limit;

    public GetMeetingListRequest(String searchKey1, Integer apge, Integer size) {
        if(!StringUtils.isEmpty(searchKey1)){
            this.searchkey = searchKey1;
        }
        int offset1 = (apge - 1) * size;
        offset1 = offset1 > 0 ? offset1 : 0;
        this.offset = offset1;
        this.limit = size;
    }
}

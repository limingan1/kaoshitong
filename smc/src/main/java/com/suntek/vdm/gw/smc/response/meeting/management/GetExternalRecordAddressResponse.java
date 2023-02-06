package com.suntek.vdm.gw.smc.response.meeting.management;


import lombok.Data;

import java.util.List;

@Data
public class GetExternalRecordAddressResponse  {
    /**
     *录制地址列表
     */
    private List<RecordAddresses> recordAddresses;
    /**
     *会议名称
     */
    private String subject;

    class RecordAddresses{
        /**
         *Ticket，有效期3分钟，每个Ticket对应一个录播地址
         */
        private String  ticket;

        /**
         *录播地址
         */
        private String  addressItem;
    }
}

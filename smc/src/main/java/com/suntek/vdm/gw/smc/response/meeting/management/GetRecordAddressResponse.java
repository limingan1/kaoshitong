package com.suntek.vdm.gw.smc.response.meeting.management;


import lombok.Data;

import java.util.List;

@Data
public class GetRecordAddressResponse  {
    /**
     *直播地址
     */
    private String liveAddress;

    /**
     *录制地址
     */
    private List<String> recordAddresses;

    /**
     *会议名称
     */
    private String subject;

    /**
     *直播主流推流地址
     */
    private String livePushAddress;

    /**
     *辅流推流地址
     */
    private String auxPushAddress;

    /**
     *观看推流直播地址
     */
    private String playPushAddress;
}

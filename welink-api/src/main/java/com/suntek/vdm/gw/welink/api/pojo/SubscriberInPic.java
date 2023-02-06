package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
public class SubscriberInPic {
    /**
     * 多画面中每个画面的编号。编号从1开 始。
     * 默认值为“1”。
     */
    private Integer index;

/**
 *每个画面中与会者标识列表。
 */
    private List<String> subscriber;

    /**
     * 是否为辅流。
     * ● 0： 不是辅流 ● 1： 是辅流 默认值：0
     */
    private Integer isAssistStream;

    public SubscriberInPic(String participantId) {
        index = 1;
        subscriber = Collections.singletonList(participantId);
        isAssistStream = 0;
    }
}
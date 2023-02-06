package com.suntek.vdm.gw.welink.api.pojo;

import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class RestMixedPictureBody {
    /**
     * 是否为手工设置多画 面。
     * ● 0： 系统自动多画 面；
     * ● 1： 手工设置多画 面。
     */
    private Integer manualSet;

    /**
     * ● Single：单画面；
     * ● Two：二画面；
     * ● Three：三画面； ● Four：四画面； ● Six：六画面；
     * ● Nine：九画面； ● Sixteen：十六画
     * 面。
     * 只针对手工设置多画面 有效。
     */
    private String imageType;

    /**
     * 子画面列表。详见表3 SubscriberInPic数据结
     */
    private List<SubscriberInPic> subscriberInPics;

    /**
     * 表示轮询间隔。单位： 秒。
     * 当同一个子画面中包含 有多个与会者视频源 时，此参数有效。
     */
    private Integer switchTime;

    /**
     * 多画面中每个画面的编号。编号从1开始。
     * 默认值为“1”。
     */
    private Integer index;

    // 每个画面中与会者标识列表。
    private String[] subscriber;
    /**
     * 是否为辅流。
     * ● 0： 不是辅流
     * ● 1： 是辅流
     * 默认值：0
     */
    private Integer isAssistStream;

    public RestMixedPictureBody(Integer manualSet, String imageType, String participantId, Integer switchTime) {
        this.manualSet = manualSet;
        this.imageType = imageType;
        this.subscriberInPics = Collections.singletonList(new SubscriberInPic(participantId));
        this.switchTime = switchTime;
    }
}
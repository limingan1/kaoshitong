package com.suntek.vdm.gw.conf.pojo;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.ConfApiUrl;
import com.suntek.vdm.gw.common.pojo.GwConferenceId;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.service.RemoteGwService;
import com.suntek.vdm.gw.common.pojo.request.ScheduleConfBrief;
import com.suntek.vdm.gw.common.pojo.request.meeting.GetConditionsMeetingRequest;
import com.suntek.vdm.gw.common.pojo.response.GetConditionsMeetingResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class ChildMeetingInfo {
    private String accessCode;
    private String smcAccessCode;
    private String id;
    private GwId gwId;
    private Long lastUpdateTime;
    private boolean isWelink = false;
    /**
     * welink同步广播会场状态标志（0：普通会场入会，1：主级联通道入会）
     */
    private Integer bSyncBroadcasOnlineFlag;
    private Integer bSyncBroadcastOnlineChannelFlag;

    private Set<GwConferenceId> childConferenceIdSet;

    public ChildMeetingInfo(String accessCode, String id, GwId gwId, String smcAccessCode) {
        this.accessCode = accessCode;
        this.id = id;
        this.gwId = gwId;
        this.smcAccessCode = smcAccessCode;
        this.childConferenceIdSet=new HashSet<>();
    }

    public String getConfCasId() {
        return accessCode;
    }

    /**
     * 加载未完成的信息
     */
    public void loadInfo(RemoteGwService remoteGwService) {
        //或者已经初始化  或者正在初始化  都直接返回
        if (initialized() || initializing()) {
            return;
        } else {
            try {
                this.lastUpdateTime = System.currentTimeMillis();//设置初始化事件
                GetConditionsMeetingRequest body = new GetConditionsMeetingRequest();
                //根据接入号（级联会议接入号）查询会议

                body.setCasConfId(getConfCasId());
                body.setSmcAccessCode(smcAccessCode);
                String responseJson = remoteGwService.toByGwId(this.gwId).post(String.format(ConfApiUrl.CONFERENCES_CONDITIONS.value(), 0, 10), body).getBody();
                GetConditionsMeetingResponse response = JSON.parseObject(responseJson, GetConditionsMeetingResponse.class);
                if (response.getContent() != null && response.getContent().size() > 0) {
                    ScheduleConfBrief childConfInfo = response.getContent().get(0);
                    this.id = childConfInfo.getId();
                }
            } catch (MyHttpException e) {

            } catch (Exception e) {
                log.error("exception", e);
            }
        }
    }
    /**
     * 是否初始化过
     *
     * @return
     */
    public boolean initialized() {
        if (id != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 是否在初始化中 默认5秒  防止频繁初始化
     *
     * @return
     */
    public boolean initializing() {
        if (lastUpdateTime == null) {
            return false;
        }
        if ((lastUpdateTime + (5 * 1000)) < System.currentTimeMillis()) {
            return false;
        }
        return true;
    }


    public boolean smcVersionIsV2() {
        if (id == null || id.length() == 36) {
            return false;
        } else {
            return true;
        }
    }
}

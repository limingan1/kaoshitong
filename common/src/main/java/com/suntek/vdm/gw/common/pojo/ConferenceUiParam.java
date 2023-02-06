package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class ConferenceUiParam {
/**
*会议Id
*/
private String id;

/**
*总会场个数
*/
private Number totalParticipantNum;

/**
*在线会场个数
*/
private Number onlineParticipantNum;

/**
*会议主题
*/
private String subject;

/**
*会议开始时间(UTC时间，格式为yyyy-MM-dd HH:mm:ss z)
*/
private String scheduleStartTime;

/**
*会议时长(单位：分钟)
*/
private Number duration;

/**
*会议横幅字幕信息
*/
private ConfTextTip confTextTip;

/**
*会议接入号
*/
private String accessCode;

/**
*是否语音会议
*/
private Boolean voice;

/**
*是否开启录播
*/
private Boolean record;

/**
*是否纯语音录播
*/
private Boolean isAudioRecord;

/**
*是否支持纪要
*/
private boolean isSupportSubtitle;

/**
*是否包含svc
*/
private boolean isContainSvc;

/**
 *是否welink vmr
 */
private Boolean isVmr;
}
package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class ConferenceParam {
/**
*会议ID
*/
private String conferenceId;

/**
*会议时长
*/
private Number duration;

/**
*在线会场个数
*/
private Number onlineNum;

/**
*总会场个数
*/
private Number totalNum;

/**
*总举手数
*/
private Number handUpNum;
}
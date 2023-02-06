package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class MultiConferenceServiceReq {
/**
*会议策略（可选）
*/
private ConferencePolicyReq conferencePolicySetting;

/**
*会议能力（可选）
*/
private ConferenceCapabilityReq conferenceCapabilitySetting;

/**
*主服务区ID(36字符)（可选）
*/
private String mainServiceZoneId;

/**
*主MCUId（1~36字符）（可选）
*/
private String mainMcuId;

/**
*主MCU名称（1~64字符）（可选）
*/
private String mainMcuName;
}
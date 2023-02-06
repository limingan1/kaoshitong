package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ParticipantDetail {
/**
*会场基本参数
*/
private ParticipantGeneralParam generalParam;

/**
*会场状态
*/
private ParticipantState state;

/**
*音视频能力集
*/
private MediaCapabilitySet capabilitySet;

/**
*TP会场状态
*/
private List<ParticipantState> subTpState;




}
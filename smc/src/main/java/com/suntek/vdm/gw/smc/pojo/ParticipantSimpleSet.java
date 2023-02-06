package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ParticipantSimpleSet {
/**
*参加会场数目
*/
private Integer size;

/**
*会场参数信息
*/
private List<ParticipantSimpleParam> participantSimpleList;
}
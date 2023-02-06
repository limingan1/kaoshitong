package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

import java.util.List;

@Data
public class ParticipantSimpleParam {
/**
*会场ID
*/
private String id;

/**
*媒体能力集
*/
private String name;

/**
*TP子屏简单参数
*/
private List<ParticipantSimpleParam> subTpSimpleList;
}
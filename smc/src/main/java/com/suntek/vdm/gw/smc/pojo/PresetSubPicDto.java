package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class PresetSubPicDto {
/**
*子画面名称
*/
private int name;

/**
*子画面号码
*/
private String uri;

/**
*子画面id,预约完会议后生成
*/
private String participantId;

/**
*
*/
private int streamNumber;
}
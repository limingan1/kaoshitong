package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class PresetMultiPicDto {
/**
*多画面名称
*/
private String name;

/**
*多画面数
*/
private int picNum;

/**
*多画面模式
*/
private boolean mode;

/**
*预置为多画面时，是否自动广播多画面
*/
private boolean autoBroadCast;

/**
*该组多画面是否启动声控
*/
private boolean autoVoiceActive;

/**
*该组多画面是否自动生效
*/
private boolean autoEffect;

/**
*预置多画面轮询请求DTO
*/
private PresetMultiPicRollDto presetMultiPicRolls;
}
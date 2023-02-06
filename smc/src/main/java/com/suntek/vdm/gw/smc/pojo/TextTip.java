package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class TextTip {
/**
*内容
*/
private String content;

/**
*操作类型
*/
private String opType;

/**
*字幕，横幅，短消息
*/
private String type;

/**
*位置
*/
private int disPosition;

/**
*效果
*/
private int displayType;
}
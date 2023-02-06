package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

@Data
public class BackupParticipant {
/**
*ID
*/
private String id;

/**
*标识
*/
private String uri;

/**
*名称
*/
private String name;

/**
*终端类型
*/
private String terminalType;

/**
*编解码类型
*/
private String mediaType;

/**
*当前是否进行过主备倒换
*/
private boolean isSwitchOver;
}
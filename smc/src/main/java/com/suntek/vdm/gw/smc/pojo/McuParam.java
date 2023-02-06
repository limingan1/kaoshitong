package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

import java.util.List;

@Data
public class McuParam {
/**
*支持会场多画面
*/
private boolean supportSiteMultiPicture;

/**
*支持纯转发
*/
private boolean supportForward;

/**
*是否支持SVC
*/
private boolean supportSvc;

/**
*是否支持AI字幕
*/
private boolean supportSubtitle;

/**
*是否支持会议纪要
*/
private boolean supportMinutes;

/**
*全适配多画面模式接口
*/
private List<Integer> multiPictureModeList;

/**
*svc多画面模式接口
*/
private List<Integer> svcMultiPictureModeList;
}
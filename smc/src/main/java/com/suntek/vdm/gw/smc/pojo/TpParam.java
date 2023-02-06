package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class TpParam {
/**
*左屏幕号码
*/
private String leftUri;

/**
*右屏幕号码
*/
private String rightUri;

/**
*左屏幕id
*/
private String leftId;

/**
*右屏幕id
*/
private String rightId;
}
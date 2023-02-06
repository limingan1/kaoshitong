package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

@Data
public class PresetMultiPicRollDto {
/**
*时间间隔
*/
private int interval;

/**
*多画面列表
*/
private PresetSubPicDto subPicList;
}
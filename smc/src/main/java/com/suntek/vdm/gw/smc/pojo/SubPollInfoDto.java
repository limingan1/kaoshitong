package com.suntek.vdm.gw.smc.pojo;

import lombok.Data;

import java.util.List;

@Data
public class SubPollInfoDto {
/**
*间隔时间
*/
private int interval;

/**
*会场列表
*/
private List<String> participantIds;
}
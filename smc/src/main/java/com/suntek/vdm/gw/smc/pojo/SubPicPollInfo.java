package com.suntek.vdm.gw.smc.pojo;

import com.suntek.vdm.gw.common.pojo.SubPic;
import lombok.Data;

import java.util.List;

@Data
public class SubPicPollInfo {
    private Integer interval;
    private List<SubPic> participantIds;
}

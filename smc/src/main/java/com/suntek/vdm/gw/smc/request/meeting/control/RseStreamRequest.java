package com.suntek.vdm.gw.smc.request.meeting.control;


import lombok.Data;

@Data
public class RseStreamRequest  {
    /**
     * 推流操作，0：停止推流，1：开启推流
     */
    private  int   pushStreamOpType;
}

package com.suntek.vdm.gw.conf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.sf.jsqlparser.expression.DateTimeLiteralExpression;

@Data
@AllArgsConstructor
public class CallCascadeChannelInfo {
    String conferenceId;
    String pId;
    long startTime;
}

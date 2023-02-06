package com.suntek.vdm.gw.common.pojo;

import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CasChannelParameter {
    private String confId;
    private Integer index;
    private CascadeParticipantDirection direction;


    public static CasChannelParameter valueOf(String str) {
        String[] parameter = str.split(CoreConfig.PARTICIPANT_SIGN);
        if (parameter.length < 3) {
            return null;
        }
        return new CasChannelParameter(parameter[0], Integer.parseInt(parameter[1]), CascadeParticipantDirection.valueOf(parameter[2]));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(confId);
        sb.append(CoreConfig.PARTICIPANT_SIGN);
        sb.append(index);
        sb.append(CoreConfig.PARTICIPANT_SIGN);
        sb.append(direction);
        return sb.toString();
    }

}

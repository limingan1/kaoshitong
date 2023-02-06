package com.suntek.vdm.gw.common.pojo;

import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;
import com.suntek.vdm.gw.common.enums.CascadeParticipantType;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CascadeParticipantParameter {
    private GwId gwId;
    private Integer index;//064+max
    private CascadeParticipantDirection direction;


    public static CascadeParticipantParameter valueOf(String value) {
        if (value == null) {
            return null;
        }

        String[] parameter = value.split("@");
        return new CascadeParticipantParameter(GwId.valueOf(parameter[0]), Integer.parseInt(parameter[1]), CascadeParticipantDirection.valueOf(Integer.valueOf(parameter[2])));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(gwId.toString());
        sb.append("@");
        sb.append(index);
        sb.append("@");
        sb.append(direction.getValue());
        return sb.toString();
    }


    public String toSmcV2String() {
        StringBuilder sb = new StringBuilder();
        sb.append("*");
        sb.append(gwId.getAreaCode());
        sb.append("*");
        sb.append(index);
        sb.append("*");
        sb.append(direction.getValue());
        return sb.toString();
    }

    public CascadeParticipantType getCascadeParticipantType() {
        GwIdType gwIdType = gwId.getGwIdType();
        switch (gwIdType) {
            case V1:
                return CascadeParticipantType.H323;
            case V2:
            case V3:
                return CascadeParticipantType.SIP;
            default:
                return CascadeParticipantType.SIP;
        }
    }

    /**
     * 获取反方向
     *
     * @return
     */
    public CascadeParticipantDirection getOppositeDirection() {
        if (this.direction.equals(CascadeParticipantDirection.DOWN)) {
            return CascadeParticipantDirection.UP;
        }
        return CascadeParticipantDirection.DOWN;
    }

    public boolean isMain() {
        return 0 == index;
    }
}



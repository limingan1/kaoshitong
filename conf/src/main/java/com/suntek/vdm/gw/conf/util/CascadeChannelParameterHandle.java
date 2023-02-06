package com.suntek.vdm.gw.conf.util;

import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.enums.CascadeParticipantDirection;

public class CascadeChannelParameterHandle {
    private static final String FORMATSTRING = "%02d";

    public static String getUri(String accessCode, int index, GwId localGwId, CascadeParticipantDirection cascadeParticipantDirection) {
//        if (SystemConfiguration.smcVersionIsV3()) {
            StringBuilder sb = new StringBuilder(accessCode);
            if (index > 0) {
                sb.append("**");
                sb.append(localGwId.getAreaCode());
                sb.append(String.format(FORMATSTRING,index));
                sb.append(cascadeParticipantDirection.getValue());
            }
            return sb.toString();
//        } else {
//            return accessCode;
//        }
    }

    public static String getCustomCallingNum(String accessCode, int index, SmcVersionType remoteType, CascadeParticipantDirection remoteDirection, GwId remoteGwId) {
        StringBuilder sb = new StringBuilder(accessCode);
        if (index > 0 || !SmcVersionType.V3.equals(remoteType)) {
            sb.append("**");
            sb.append(remoteGwId.getAreaCode());
            sb.append(String.format(FORMATSTRING,index));
            sb.append(remoteDirection.getValue());
        }
        return sb.toString();
    }

}

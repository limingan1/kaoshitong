package com.suntek.vdm.gw.welink.api.request;

import com.suntek.vdm.gw.welink.api.pojo.RestMuteReqBody;
import lombok.Data;

@Data
public class SetMuteRequest  extends RestMuteReqBody {
    public SetMuteRequest(int mute,Integer allow) {
        super(mute, allow);
    }
}

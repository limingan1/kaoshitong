package com.suntek.vdm.gw.conf.context;

import com.suntek.vdm.gw.conf.service.impl.BaseServiceImpl;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.conf.service.SmcMeetingRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SmcMeetingRoomContext extends BaseServiceImpl {
    @Autowired
    private Map<String, SmcMeetingRoomService> map;

    public SmcMeetingRoomService get(GwId gwId) {
        NodeBusinessType type=getNodeBusinessType(gwId);
        String name = "MeetingRoomServiceImpl";
        switch (type) {
            case SMC: {
                return map.get("smc" + name);
            }
            case WELINK:
            case CLOUDLINK: {
                return map.get("welink" + name);
            }
            default:
                break;
        }
        log.error("{} not found by type:{}", name, type.name());
        return null;
    }
}

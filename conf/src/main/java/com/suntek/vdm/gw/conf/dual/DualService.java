package com.suntek.vdm.gw.conf.dual;

import com.suntek.vdm.gw.common.pojo.request.DualHostDto;
import com.suntek.vdm.gw.common.util.dual.Host;

public interface DualService {
    DualHostDto dualHostReady(DualHostDto dualHostDto);

    void start();

    void statrService();

    void excureCmd(boolean isMaster, Host host);
}

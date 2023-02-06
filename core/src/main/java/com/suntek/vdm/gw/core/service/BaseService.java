package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.common.pojo.BaseState;

public interface BaseService {
     BaseState success(String msg);
     BaseState success();
     BaseState fail(String msg);
     BaseState fail();
     BaseState state(boolean i);
}

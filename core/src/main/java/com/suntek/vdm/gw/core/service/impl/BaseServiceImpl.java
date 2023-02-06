package com.suntek.vdm.gw.core.service.impl;

import com.suntek.vdm.gw.common.pojo.BaseState;
import com.suntek.vdm.gw.core.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class BaseServiceImpl  implements BaseService {
    /**
     * 返回成功的业务信息(含消息)
     *
     * @param msg
     * @return 业务对象。
     */
    @Override
    public  BaseState success(String msg) {
        BaseState baseState = new BaseState();
        baseState.setCode(1);
        baseState.setMsg(msg);
        return baseState;
    }

    /**
     * 返回成功的业务信息(不含消息)
     *
     * @return 业务对象
     */
    @Override
    public  BaseState success() {
        BaseState baseState = new BaseState();
        baseState.setCode(1);
        baseState.setMsg("操作成功");
        return baseState;
    }

    /**
     * 返回失败的业务信息(含消息)
     *
     * @param msg
     * @return 业务对象
     */
    @Override
    public  BaseState fail(String msg) {
        BaseState baseState = new BaseState();
        baseState.setCode(0);
        baseState.setMsg(msg);
        return baseState;
    }

    /**
     * 返回失败的业务信息(不含消息)
     *
     * @return 业务对象
     */
    @Override
    public BaseState fail() {
        BaseState baseState = new BaseState();
        baseState.setCode(0);
        baseState.setMsg("操作失败");
        return baseState;
    }

    /**
     * 根据状态返回业务消息
     *
     * @param i 数据行数
     * @return 业务对象
     */
    @Override
    public  BaseState state(boolean i) {
        if (i) {
            return success();
        } else {
            return fail();
        }
    }
}

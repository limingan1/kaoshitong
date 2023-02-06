package com.suntek.vdm.gw.common.util;


import com.suntek.vdm.gw.common.pojo.BaseState;

public class ResultHelper {
    /**
     * 返回成功的业务信息(含消息)
     *
     * @param msg
     * @return 业务对象。
     */
    public static BaseState success(String msg) {
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
    public static BaseState success() {
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
    public static BaseState fail(String msg) {
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
    public static BaseState fail() {
        BaseState baseState = new BaseState();
        baseState.setCode(0);
        baseState.setMsg("操作失败");
        return baseState;
    }

}

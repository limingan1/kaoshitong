package com.suntek.vdm.gw.common.enums;

public enum CoreApiUrl {
    KEEP_ALIVE("/conf-portal/tokens"),
    SEND_LOW_TREE("/node/sendLowTree"),
    SEND_TOP_TREE("/node/sendTopTree"),
    REMOTE_NODE_UPDATE("/node/remoteNodeUpdate"),
    GET_TOKEN("/conf-portal/tokens"),
    GET_NODE_TOKEN("/node/tokens"),
    CHECK_NODE_ADD_REMOTE("/node/check/add/remote"),
    CHECK_NODE_UPDATE_REMOTE("/node/check/update/remote"),
    CHECK_NODE_ADD_LOCAL("/node/check/add/local"),
    CHECK_NODE_UPDATE("/node/check/update"),
    NODE_REMOTE_INFO("/node/remote/info"),
    GW_WEBSOCKET("/gw/websocket/"),
    GET_LICENSE("/license/getLicense"),
    CHANGE_LICENSE("/license/change"),
    DELETE_WARNING_REPORT("/warningreport/delete"),
    CASCADE_RESUME_LOW_SUBSCRIBE("/conf-portal/cascade/resumeLowSubscribe/%s");

    private String value;

    private CoreApiUrl(String value) {     //必须是private的，否则编译错误
        this.value = value;
    }

    public String value() {
        return this.value;
    }

}

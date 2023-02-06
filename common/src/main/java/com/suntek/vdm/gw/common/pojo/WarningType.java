package com.suntek.vdm.gw.common.pojo;

public enum WarningType {
    REMOTE_NODE_NOT_CONFIG,     //请检查远端节点是否配置本节点信息
    NODE_LOGIN_ERROR,           //节点登录异常，请检查账号信息
    HTTP_CONNECT_ERROR,         //节点网络连接异常，请检查网络状态
    REMOTE_SERVER_ERROR,        //节点服务异常，请检查远端服务状态
    LOCAL_NODE_NOT_CONFIG       //本级节点没有配置
}

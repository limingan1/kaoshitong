package com.suntek.vdm.gw.common.enums;

import com.alibaba.fastjson.JSONObject;

public enum GwErrorCode {
    UNKNOWN_ERROR("GW", "502", "UNKNOWN_ERROR", "未知错误"),
    NODE_CONFIG_NAME_EXISTS("GW", "1001", "NODE_CONFIG_NAME_EXISTS", "节点名称已存在"),
    NODE_CONFIG_AREA_CODE_EXISTS("GW", "1002", "NODE_CONFIG_AREA_CODE_EXISTS", "地区编码已存在"),
    NODE_CONFIG_IP_EXISTS("GW", "1003", "NODE_CONFIG_IP_EXISTS", "IP已存在"),//
    NODE_CONFIG_ACCOUNT_PASSWORD_ERROR("GW", "1004", "NODE_CONFIG_ACCOUNT_PASSWORD_ERROR", "用户名或密码错误"),
    NODE_CONFIG_IP_ERROR("GW", "1005", "NODE_CONFIG_IP_ERROR", "IP地址错误"),
    NODE_CONFIG_NODE_EXISTS("GW", "1006", "NODE_CONFIG_NODE_EXISTS", "节点已存在"),
    NODE_CONFIG_REMOTE_HAS_TOP("GW", "1007", "NODE_CONFIG_REMOTE_HAS_TOP", "远端已经有上级"),
    NODE_CONFIG_REMOTE_NAME_EXISTS("GW", "1008", "NODE_CONFIG_REMOTE_NAME_EXISTS", "名称和远端重复"),
    NODE_CONFIG_REMOTE_AREA_CODE_EXISTS("GW", "1009", "NODE_CONFIG_REMOTE_AREA_CODE_EXISTS", "地区编码和远端重复"),
    NODE_CONFIG_NETWORK_ERROR("GW", "1010", "NODE_CONFIG_NETWORK_ERROR", "网络错误"),
    NO_LICENSE_ERROR("GW", "1011", "PLEASE_CHECK_LICENSE", "请检查License"),
    NO_LOCAL_NODE_ERROR("GW", "1012", "LOCAL_NODE_NOT_EXISTS", "本地节点未配置"),
    NODE_CONFIG_HAD_CHILD("GW", "1013", "NODE_CONFIG_HAD_CHILD", "节点存在子节点"),
    NAME_EXISTS("GW", "1014", "NAME_EXISTS", "名称已存在"),
    NODE_CONFIG_NOT_EXISTS("GW", "1015", "NODE_CONFIG_NOT_EXISTS", "节点不存在"),
    NO_PERMISSION("GW", "1016", "NO_PERMISSION","没有访问权限"),
    ORGID_EXISTS("GW", "1017", "ORGID_EXISTS", "组织已存在"),
    USERNAME_EXISTS("GW", "1018", "USERNAME_EXISTS", "用户名已存在"),
    CONFERENCE_NOT_EXIST("SMC","0x30010004","CONFERENCE_NOT_EXIST","会议不存在"),
    CASCADE_CHANNEL_ALLOCATION_FAILED("GW", "0x30010006", "CASCADE_CHANNEL_ALLOCATION_FAILED","级联通道分配失败"),
    NO_WELINK_LICENSE_ERROR("GW", "1011", "PLEASE_CHECK_WELINK_LICENSE","请检查license"),
    WELINK_NO_CALID_VIDEO_SOURCE("SMC2.0", "1345323090", "WELINK_NO_CALID_VIDEO_SOURCE",""),
    LOWER_PARTICIPANT_CAN_NOT_FELLOW("GW", "0x30010007", "LOWER_PARTICIPANT_CAN_NOT_FELLOW","非本级会场不允许观看"),
    PLEASE_CHECK_CLIENT_ID("GW", "1020", "PLEASE_CHECK_CLIENT_ID","请检查client_id或client_secret"),
    PLEASE_CHECK_URL("GW", "1021", "PLEASE_CHECK_URL","请检查url"),
    CANT_CONFIGURED_VM_NODE_ORG("GW", "1019", "CANT_CONFIGURED_VM_NODE_ORG","不能同时配置虚拟节点和分职"),
    NO_FREE_VMRID("GW", "1020", "NO_FREE_VMRID","没有可用的Vmr会议Id"),
    ACCOUNT_LOCKED("GW", "1022", "ACCOUNT_LOCKED","账号被锁定"),
    ;
    private String errorType;
    private String errorNo;
    private String errorDesc;
    private String errorDesc_zh;

    GwErrorCode(String errorType, String errorNo, String errorDesc, String errorDesc_zh) {
        this.errorType = errorType;
        this.errorNo = errorNo;
        this.errorDesc = errorDesc;
        this.errorDesc_zh = errorDesc_zh;
    }


    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("errorType", errorType);
        jsonObject.put("errorNo", errorNo);
        jsonObject.put("code", errorNo);
        jsonObject.put("errorDesc", errorDesc);
        jsonObject.put("errorDesc_zh", errorDesc_zh);
        return jsonObject.toJSONString();
    }
}
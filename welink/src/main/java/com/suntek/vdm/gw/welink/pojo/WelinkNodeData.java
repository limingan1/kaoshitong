package com.suntek.vdm.gw.welink.pojo;

import com.suntek.vdm.gw.common.pojo.GwId;
import lombok.Data;

@Data
public class WelinkNodeData {
    private String ip;
    private String username;
    private String password;
    private String enType;
    private String id;
    private String name;
    private String areaCode;
    private String vmrConfId;
    private String clientId;
    private String clientSecret;
    private String addressBookUrl;

    private GwId gwId;

}

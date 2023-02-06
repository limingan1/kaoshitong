package com.suntek.vdm.gw.core.api.request.node;

import lombok.Data;

@Data
public class UpdateNodeRequest {
    private String id;
    private String name;
    private String areaCode;
    private String ip;
    private int ssl;
    private String vmrConfId;
    private String username;
    private String password;
    private Integer permissionSwitch;

    private String clientId;
    private String clientSecret;
    private String addressBookUrl;

    public void setPassword(String password) {
        if ("******".equals(password)){
            this.password=null;
        }else{
            this.password = password;
        }
    }

    @Override
    public String toString() {
        return "UpdateNodeRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", ip='" + ip + '\'' +
                ", ssl=" + ssl +
                ", username='" + username + '\'' +
                ", password='" + "******" + '\'' +
                '}';
    }
}

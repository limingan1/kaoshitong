package com.suntek.vdm.gw.core.api.request.node;

import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import lombok.Data;

@Data
public class AddNodeRequest{
    private String name;
    private String areaCode;
    private Integer type;
    private Integer businessType;
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

    public Integer getBusinessType() {
        if (businessType==null){
            businessType=NodeBusinessType.SMC.value();
        }
        return businessType;
    }

    @Override
    public String toString() {
        return "AddNodeRequest{" +
                "name='" + name + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", type=" + type +
                ", ip='" + ip + '\'' +
                ", ssl=" + ssl +
                ", username='" + username + '\'' +
                ", password='" + "******" + '\'' +
                '}';
    }
}

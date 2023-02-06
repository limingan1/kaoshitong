package com.suntek.vdm.gw.core.api.request.vm;

import lombok.Data;


@Data
public class AddVmNodeRequest {
    private String parentId;

    private String name;

    private String areaCode;

    private String username;

    private String password;

    private String orgId;

    private Integer permissionSwitch;



    public void setPassword(String password) {
        if ("******".equals(password)){
            this.password=null;
        }else{
            this.password = password;
        }
    }

    @Override
    public String toString() {
        return "AddVmNodeRequest{" +
                "parentId='" + parentId + '\'' +
                ", name='" + name + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", username='" + username + '\'' +
                ", password='******'" +
                ", orgId='" + orgId + '\'' +
                ", permissionSwitch=" + permissionSwitch +
                '}';
    }
}

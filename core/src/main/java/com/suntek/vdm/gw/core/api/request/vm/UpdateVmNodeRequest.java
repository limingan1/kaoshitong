package com.suntek.vdm.gw.core.api.request.vm;

import lombok.Data;

@Data
public class UpdateVmNodeRequest {
    private String id;

    private String parentId;

    private String name;

    private String areaCode;

    private String username;

    private String password;

    private Integer permissionSwitch;

    private String orgId;

    public void setPassword(String password) {
        if ("******".equals(password)){
            this.password=null;
        }else{
            this.password = password;
        }
    }

    @Override
    public String toString() {
        return "UpdateVmNodeRequest{" +
                "id='" + id + '\'' +
                ", parentId='" + parentId + '\'' +
                ", name='" + name + '\'' +
                ", areaCode='" + areaCode + '\'' +
                ", username='" + username + '\'' +
                ", password='******'" +
                ", permissionSwitch=" + permissionSwitch +
                ", orgId='" + orgId + '\'' +
                '}';
    }
}

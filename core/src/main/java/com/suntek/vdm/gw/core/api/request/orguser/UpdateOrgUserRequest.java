package com.suntek.vdm.gw.core.api.request.orguser;

import lombok.Data;

@Data
public class UpdateOrgUserRequest {
    private String id;

    private String nodeId;

    private String name;

    private String username;

    private String password;

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
        return "UpdateNodeRequest{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", orgId=" + orgId +
                ", username='" + username + '\'' +
                ", password='" + "******" + '\'' +
                '}';
    }
}

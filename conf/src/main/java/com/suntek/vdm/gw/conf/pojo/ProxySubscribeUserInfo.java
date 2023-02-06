package com.suntek.vdm.gw.conf.pojo;

import com.suntek.vdm.gw.conf.enumeration.SubscribeUserType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProxySubscribeUserInfo {
    /**
     * 回推路径
     */
    private String backDestination;
    /**
     * 订阅的客户端用户Id  用于回推
     */
    private String user;
    /**
     * 用户类型
     */
    private SubscribeUserType type;


    @Override
    public String toString() {
        return getUser();
    }

    @Override
    public boolean equals(Object obj) {
        ProxySubscribeUserInfo o = (ProxySubscribeUserInfo) obj;
        if (this.getUser().equals(o.getUser())) {
            if (this.getType().equals(o.getType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(backDestination);
        sb.append(user);
        sb.append(type);
        char[] charArr = sb.toString().toCharArray();
        int hash = 0;
        for (char c : charArr) {
            hash = hash * 131 + c;
        }
        return hash;
    }

}

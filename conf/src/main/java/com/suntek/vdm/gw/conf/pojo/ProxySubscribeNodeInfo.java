package com.suntek.vdm.gw.conf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProxySubscribeNodeInfo {

    /**
     *
     */
    private String nodeId;
    /**
     * 回推路径
     */
    private String backDestination;

    @Override
    public String toString() {
        return getNodeId();
    }

    @Override
    public boolean equals(Object obj) {
        ProxySubscribeNodeInfo o = (ProxySubscribeNodeInfo) obj;
        if (this.getNodeId().equals(o.getNodeId())) {
            if (this.getBackDestination().equals(o.getBackDestination())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(backDestination);
        sb.append(nodeId);
        char[] charArr = sb.toString().toCharArray();
        int hash = 0;
        for (char c : charArr) {
            hash = hash * 131 + c;
        }
        return hash;
    }

}

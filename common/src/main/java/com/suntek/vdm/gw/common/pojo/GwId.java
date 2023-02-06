package com.suntek.vdm.gw.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GwId {
    private String nodeId;
    private String areaCode;

    public GwId(String value) {
        String[] parameter = value.split("\\*");
        nodeId = parameter[0];
        if (parameter.length >= 2) {
            areaCode = parameter[1];
        }
    }


    public static GwId valueOf(String value) {
        if (value == null) {
            return null;
        }
        String[] parameter = value.split("\\*");
        return new GwId(parameter[0], parameter[1]);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nodeId);
        sb.append("*");
        sb.append(areaCode);
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;//地址相等
        }
        GwId gwId = (GwId) obj;
        if (gwId.getNodeId().equals(nodeId) || gwId.getAreaCode().equals(areaCode)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (nodeId == null ? 0 : nodeId.hashCode());
        result = 31 * result + (areaCode == null ? 0 : areaCode.hashCode());
        return result;
    }


    public GwIdType getGwIdType() {
        return GwIdType.get(nodeId);
    }

    /**
     * 非完整状态
     *
     * @return
     */
    public boolean inComplete() {
        if (getGwIdType().equals(GwIdType.V1)) {
            return true;
        }
        return false;
    }
}

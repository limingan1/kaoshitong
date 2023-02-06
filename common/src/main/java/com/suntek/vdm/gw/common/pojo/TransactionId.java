package com.suntek.vdm.gw.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TransactionId {
    private TransactionType type;
    private String id;


    @Override
    public String toString() {
        return "TransactionId{" +
                "type=" + type +
                ", id='" + id + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        TransactionId o = (TransactionId) obj;
        if (this.type.equals(o.getType())&&this.id.equals(o.getId())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.name());
        sb.append(id);
        char[] charArr = sb.toString().toCharArray();
        int hash = 0;
        for (char c : charArr) {
            hash = hash * 131 + c;
        }
        return hash;
    }
}

package com.suntek.vdm.gw.conf.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class CascadeChannel {
    /**
     * 主叫号码
     */
    private String callingNumberMain;
    private String callingNumberRemark;

    /**
     * 被叫号码
     */
    private String calledNumberMain;

    private String calledNumberRemark;

    private int index;


    public String CalledNumberToUri() {
        StringBuilder sb = new StringBuilder();
        if (calledNumberMain != null) {
            sb.append(calledNumberMain);
            if (index > 0) {
                sb.append("*");
                sb.append(index);
            }
            if (calledNumberRemark != null) {
                sb.append("*");
                sb.append(calledNumberRemark);
            }
            return sb.toString();
        }
        return null;
    }

    public String CallingNumberToUri() {
        StringBuilder sb = new StringBuilder();
        if (callingNumberMain != null) {
            sb.append(callingNumberMain);
            if (index > 0) {
                sb.append("*");
                sb.append(index);
            }
            if (callingNumberRemark != null) {
                sb.append("*");
                sb.append(callingNumberRemark);
            }
            return sb.toString();
        }
        return null;
    }
}

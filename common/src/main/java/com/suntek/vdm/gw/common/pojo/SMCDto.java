package com.suntek.vdm.gw.common.pojo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Data
public class SMCDto implements Serializable {

    private static final long serialVersionUID = 6883683351763818889L;

    private int code;

    private String address;

    private String protocol;

    private String port;

    private String domain;

    @Override
    public String toString() {
        return "SMCDto{" +
                ", address='" + address + '\'' +
                ", password='" + "******" + '\'' +
                ", protocol='" + protocol + '\'' +
                ", port='" + port + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}

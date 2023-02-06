package com.suntek.vdm.gw.welink.api.response;

import com.suntek.vdm.gw.welink.api.enumeration.Status;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
@Data
public class WelinkSite implements Cloneable{
    private String id;
    private Integer statusCode;
    private String account;
    private String name;
    private String englishName;
    private String email;
    private String phone;
    private String deptName;
    private String number;
    private String title;
    private String description;
    private String vmrId;
    private long updateTime;
    private Boolean isHardTerminal;
    private String hidePhone;
    private Integer hideType;
    private String type;
    private String userType;
    private List<String> deptFullName;
    private List<String> deptCodes;
    private Boolean attend;
    private List<String> pid = new ArrayList<>(16);
    private Status status = Status.ON_NONE;
    private Boolean share;
    private Boolean state = false;
    private Boolean ignoreSent = true;

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

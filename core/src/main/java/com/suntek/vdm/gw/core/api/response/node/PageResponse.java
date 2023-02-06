package com.suntek.vdm.gw.core.api.response.node;


import com.suntek.vdm.gw.common.pojo.node.NodeStatus;
import lombok.Data;

import java.util.Date;

@Data
public class PageResponse  {
    private String id;

    private String name;

    private String areaCode;
    private String childAreaCod;
    private Integer type;
    private Integer businessType;
    private String ip;
    private int ssl;
    private String username;
    private Date createTime;
    private Date updateTime;
    private Integer permissionSwitch;
    //welink节点使用
    private String vmrConfId;
    private String clientId;
    private String clientSecret;
    private String addressBookUrl;
    //welink节点使用 end...

    private NodeStatus nodeStatus;



}


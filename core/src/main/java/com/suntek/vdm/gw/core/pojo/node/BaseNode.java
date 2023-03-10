package com.suntek.vdm.gw.core.pojo.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseNode {
    private UUID id ;
    private String name ;
    private String areaCode ;
    private String pAreaCode ;
    private int status;
}

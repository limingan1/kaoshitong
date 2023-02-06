package com.suntek.vdm.gw.core.pojo.node;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;


@Data
@NoArgsConstructor
public class SmcNode extends  BaseNode{
    private String ip ;
    private String msg ;

    public SmcNode(String name, String code, String pCode,String ip) {
        super(UUID.randomUUID(),name, code, pCode, 1);
        this.ip = ip;
    }
}

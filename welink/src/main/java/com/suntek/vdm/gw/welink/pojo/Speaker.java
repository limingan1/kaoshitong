package com.suntek.vdm.gw.welink.pojo;

import lombok.Data;

import java.io.Serializable;

@Data
public class Speaker implements Serializable {

    private String pid;

    private String name;

    private String speakingVolume;

}

package com.suntek.vdm.gw.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogEntity implements Serializable {

    private static final long serialVersionUID = 6880539814336562589L;

    private String logId;

    private Date logTime;

    private String logLeave;

    private String logSource;

    private String logDescription;

    private String logDescriptionEn;

    private String logType;

    public LogEntity(String logDescription,String logDescriptionEn,String logSource) {
        this.logDescription = logDescription;
        this.logDescriptionEn = logDescriptionEn;
        this.logSource = logSource;
    }
}

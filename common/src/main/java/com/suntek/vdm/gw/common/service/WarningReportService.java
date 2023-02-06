package com.suntek.vdm.gw.common.service;

import com.suntek.vdm.gw.common.pojo.WarningReportDto;
import com.suntek.vdm.gw.common.pojo.WarningType;

public interface WarningReportService {

    void checkLocalNodeConfig();

    void addWarningReport(WarningReportDto warningReportDto);

    void deleteWarningReport(WarningType warningType);
}

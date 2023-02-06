package com.suntek.vdm.gw.core.service;

public interface StartService {
    boolean checkDataSource();
    void initSmcConfig();
    void loadNodeByDatabase();
    void initDatabase();

}

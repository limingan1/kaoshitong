package com.suntek.vdm.gw.common.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.constant.Constants;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.WarningReportDto;
import com.suntek.vdm.gw.common.pojo.WarningType;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.service.WarningReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class WarningReportServiceImpl implements WarningReportService {
    @Autowired
    @Qualifier("httpServiceImpl")
    private HttpService httpService;

    private final Map<WarningType, List<String>> warningTypeMap = new ConcurrentHashMap<>();

    @Async("taskExecutor")
    @Override
    public void checkLocalNodeConfig() {

    }

    @Async("taskExecutor")
    @Override
    public void addWarningReport(WarningReportDto warningReportDto) {
        String url = Constants.localAddress + ":" + Constants.port + Constants.prefix + "/Omc/batchInsertVdcOmc";
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(warningReportDto);
        try {
            MultiValueMap<String, String> header = new LinkedMultiValueMap<>();
            ResponseEntity<String> res = httpService.post(url, jsonArray, header);
            String body = res.getBody();
            WarningReportDto response = JSONObject.parseObject(body, WarningReportDto.class);
            saveToMap(response, warningReportDto.getWarningType());
            log.info("sent warning report success");
        }catch (MyHttpException e){
            log.error("send warning report error: code:{},msg:{}",e.getCode(),e.getMessage());
        }

    }

    private void saveToMap(WarningReportDto response, WarningType warningType) {
        List<String> idList = warningTypeMap.get(warningType);
        if (idList == null) {
            idList = new Vector<>();
        }
        idList.add(response.getOmcId());
        warningTypeMap.put(warningType, idList);
    }

    @Async("taskExecutor")
    @Override
    public void deleteWarningReport(WarningType warningType) {
        List<String> idList = warningTypeMap.get(warningType);
        if (idList != null && !idList.isEmpty()) {
            //删除告警
            String[] ids = new String[idList.size()];
            for (int i = 0; i < idList.size(); i++) {
                ids[i] = idList.get(i);
            }
            try {
                deleteWarningReport(ids);
                warningTypeMap.remove(warningType);
            } catch (MyHttpException e) {
                e.printStackTrace();
                log.error("delete warning report error: code:{},msg:{}",e.getCode(),e.getMessage());
            }
        }
    }

    private void deleteWarningReport(String[] ids) throws MyHttpException {
        String url = Constants.localAddress + ":" + Constants.port + Constants.prefix + "/Omc/deleteVdcOmc";
        httpService.delete(url, ids, new LinkedMultiValueMap<>());
        log.info("delete warning report success，ids：{}", Arrays.stream(ids).findFirst());
    }
}

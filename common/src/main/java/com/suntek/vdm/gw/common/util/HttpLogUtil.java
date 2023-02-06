package com.suntek.vdm.gw.common.util;

import com.suntek.vdm.gw.common.pojo.HttpPrintConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HttpLogUtil {

    public static Map<String, HttpPrintConfig> HttpPrintConfigMap = new HashMap<String, HttpPrintConfig>() {
        {
            put("conf-portal/tokens", new HttpPrintConfig(false, false));
            put("conf-portal/addressbook/rooms", new HttpPrintConfig(true, false));
        }
    };


    public static void request(String url, String method, String body, String form, String formIp, String to) {
        try {
            String regexPassword = "(?<=(\"password\":\")).*?(?=(\"))";
            String regexToken = "(?<=(\"token\":\")).*?(?=(\"))";
            if(body != null){
                body = body.replaceAll(regexPassword, "******");
                body = body.replaceAll(regexToken, "******");
            }
        } catch (Exception e) {
            log.error("body error:{}", e.getMessage());
        }
        log.info("{}->{} {} [{}]->{} {}", form, to, method, formIp, url, body);
    }

    public static void response(int statusCode, Object body) {
        log.info("response:{} {}", statusCode, body);
    }

    public static HttpPrintConfig filter(String url) {
        try {
            url = url.split("\\?")[0];//去点后面的参数
            String[] urlSuffixSplit = url.split("/");
            int index = 0;
            if (url.contains("http://") || url.contains("https://")) {
                index = 3;
            }
            if (StringUtils.isEmpty(urlSuffixSplit[0])) {
                index = 1;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = index; i < urlSuffixSplit.length; i++) {
                sb.append(urlSuffixSplit[i]);
                if (i != urlSuffixSplit.length - 1) {
                    sb.append("/");
                }
            }
            String urlSuffix = sb.toString();
            if (HttpPrintConfigMap.containsKey(urlSuffix)) {
                return HttpPrintConfigMap.get(urlSuffix);
            }
            HttpPrintConfig httpPrintConfig = new HttpPrintConfig(true, true);
            return httpPrintConfig;
        } catch (Exception e) {
            log.error("exception", e);
            HttpPrintConfig httpPrintConfig = new HttpPrintConfig(true, true);
            return httpPrintConfig;
        }
    }
}






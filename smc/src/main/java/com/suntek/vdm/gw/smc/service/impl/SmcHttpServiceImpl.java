package com.suntek.vdm.gw.smc.service.impl;


import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.service.impl.HttpServiceImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Service
public class SmcHttpServiceImpl extends HttpServiceImpl implements HttpService {
    public static String ip;


    public static boolean ssl=true;
    public static String prefix="/conf-portal";
    public static SmcVersionType version;

    @Override
    public ResponseEntity<String> request(String url, Object body, MultiValueMap<String, String> headers, HttpMethod method) throws MyHttpException {
        StringBuilder sb = new StringBuilder();
        sb.append(ssl ? "https://" : "http://");
        sb.append(ip);
        sb.append(prefix);
        sb.append(url);
        if (headers == null) {
            headers = new LinkedMultiValueMap<String, String>();
        }
        if (version.equals(SmcVersionType.V2)) {
            headers.add("type", "2.0");
         }
        if (body == null) {
            body = new LinkedMultiValueMap<>();
        }
        return request(sb.toString(), body, headers, method, MediaType.APPLICATION_JSON_UTF8, "S", "SMC");
    }
}

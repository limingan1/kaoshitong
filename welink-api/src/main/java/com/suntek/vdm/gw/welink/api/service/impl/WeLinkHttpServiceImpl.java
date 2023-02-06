package com.suntek.vdm.gw.welink.api.service.impl;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.service.impl.HttpServiceImpl;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Service
public class WeLinkHttpServiceImpl extends HttpServiceImpl implements HttpService {
    private static boolean ssl = true;
    public static String prefix = "/v1";

    private static String address;
    private static String username;
    private static String password;

    private ThreadLocal<Boolean> RETRY = new ThreadLocal<Boolean>();


    public static void setAddress(String address) {
        WeLinkHttpServiceImpl.address = address;
    }

    public static void setInfo(String address, String username, String password) {
        WeLinkHttpServiceImpl.address = address;
        WeLinkHttpServiceImpl.username = username;
        WeLinkHttpServiceImpl.password = password;
    }

    @Override
    public ResponseEntity<String> request(String url, Object body, MultiValueMap<String, String> headers, HttpMethod method) throws MyHttpException {
        if (RETRY.get() == null) {
            RETRY.set(false);
        } else {
            RETRY.set(true);
        }
        StringBuilder sb = new StringBuilder();
        boolean isIntegralUrl = url.contains("http");
        if (!isIntegralUrl) {
            sb.append(ssl ? "https://" : "http://");
            sb.append(address);
            sb.append(prefix);
            sb.append(url);
        }
        if (headers == null) {
            headers = new LinkedMultiValueMap<String, String>();
        }
        if (body == null) {
            body = new LinkedMultiValueMap<>();
        }
        try {
            return request(isIntegralUrl ? url : sb.toString(), body, headers, method, MediaType.APPLICATION_JSON_UTF8, "S", "WELINK");
        } catch (MyHttpException e) {
//            //返回未鉴权而且不是在重试 默认登录重发一次
//            if (e.getCode() == 401 && !(RETRY.get())) {
//                //判断URL不是登录和保活
//                if (!(url.equals("/usg/acs/auth/account") || url.equals("/usg/acs/token"))) {
//                    //先登录一次
//
//                    return request(url, body, headers, method);
//                }
//            }
            throw e;
        } finally {
            RETRY.remove();
        }
    }
    @Override
    public ResponseEntity<String> post(String url, Object body) throws MyHttpException {
        return super.post(url, body, null);
    }

    @Override
    public ResponseEntity<String> get(String url, Object body) throws MyHttpException {
        return super.get(url, body);
    }

    @Override
    public ResponseEntity<String> get(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return super.get(url, body, headers);
    }

    @Override
    public ResponseEntity<String> post(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return super.post(url, body, headers);
    }

    @Override
    public ResponseEntity<String> put(String url, Object body) throws MyHttpException {
        return super.put(url, body);
    }

    @Override
    public ResponseEntity<String> put(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return super.put(url, body, headers);
    }

    @Override
    public ResponseEntity<String> patch(String url, Object body) throws MyHttpException {
        return super.patch(url, body);
    }

    @Override
    public ResponseEntity<String> patch(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return super.patch(url, body, headers);
    }

    @Override
    public ResponseEntity<String> delete(String url, Object body) throws MyHttpException {
        return super.delete(url, body);
    }

    @Override
    public ResponseEntity<String> delete(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return super.delete(url, body, headers);
    }
}

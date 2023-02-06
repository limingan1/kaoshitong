package com.suntek.vdm.gw.common.service.impl;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.HttpPrintConfig;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.util.HttpLogUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class HttpServiceImpl implements HttpService {
    @Autowired
    @Qualifier("casTemplate")
    private RestTemplate restTemplate;

    private boolean ssl = false;

    /**
     * get请求
     *
     * @param url
     * @param body 请求参数
     * @return
     */
    @Override
    public ResponseEntity<String> get(String url, Object body) throws MyHttpException {
        return get(url, body, null);
    }

    /**
     * get请求
     *
     * @param url
     * @param body    请求参数
     * @param headers 请求头
     * @return
     */
    @Override
    public ResponseEntity<String> get(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return request(url, body, headers, HttpMethod.GET);
    }

    /**
     * post请求
     *
     * @param url
     * @param body 请求参数
     * @return
     */
    @Override
    public ResponseEntity<String> post(String url, Object body) throws MyHttpException {
        return post(url, body, null);
    }

    /**
     * post请求
     *
     * @param url
     * @param body    请求参数
     * @param headers 请求头
     * @return
     */
    @Override
    public ResponseEntity<String> post(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return request(url, body, headers, HttpMethod.POST);
    }

    /**
     * put请求
     *
     * @param url
     * @param body 请求参数
     * @return
     */
    @Override
    public ResponseEntity<String> put(String url, Object body) throws MyHttpException {
        return put(url, body, null);
    }

    /**
     * put请求
     *
     * @param url
     * @param body    请求参数
     * @param headers 请求头
     * @return
     */
    @Override
    public ResponseEntity<String> put(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return request(url, body, headers, HttpMethod.PUT);
    }


    /**
     * put请求
     *
     * @param url
     * @param body 请求参数
     * @return
     */
    @Override
    public ResponseEntity<String> patch(String url, Object body) throws MyHttpException {
        return patch(url, body, null);
    }

    /**
     * patch
     *
     * @param url
     * @param body    请求参数
     * @param headers 请求头
     * @return
     */
    @Override
    public ResponseEntity<String> patch(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return request(url, body, headers, HttpMethod.PATCH);
    }

    /**
     * delete请求
     *
     * @param url
     * @param body 请求参数
     * @return
     */
    @Override
    public ResponseEntity<String> delete(String url, Object body) throws MyHttpException {
        return delete(url, body, null);
    }

    /**
     * delete请求
     *
     * @param url
     * @param body    请求参数
     * @param headers 请求头
     * @return
     */
    @Override
    public ResponseEntity<String> delete(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException {
        return request(url, body, headers, HttpMethod.DELETE);
    }

    /**
     * 表单请求
     *
     * @param url
     * @param body    请求主体
     * @param headers 请求头
     * @param method  请求方式
     * @return
     */
    @Override
    public ResponseEntity<String> request(String url, Object body, MultiValueMap<String, String> headers, HttpMethod method) throws MyHttpException {
        if (body == null) {
            body = new LinkedMultiValueMap<>();
        }
        return request(url, body, headers, method, MediaType.APPLICATION_JSON_UTF8, "S", "S");
    }

    /**
     * http请求
     *
     * @param url
     * @param body      请求参数
     * @param headers   请求头
     * @param method    请求方式
     * @param mediaType 参数类型
     * @return
     */
    public ResponseEntity<String> request(String url, Object body, MultiValueMap<String, String> headers, HttpMethod method, MediaType mediaType, String form, String to) throws MyHttpException {
//        boolean logPrint = true;
//        String[] urlSuffixSplit = url.split("/");
//        String urlSuffix = urlSuffixSplit[urlSuffixSplit.length - 1];
//        if (FilterCache.getLog().contains(urlSuffix)) {
//            logPrint = false;
//        }
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        if (!url.contains("http")) {
            if (ssl) {
                url = "https://" + url;
            } else {
                url = "http://" + url;
            }
        }
        HttpPrintConfig httpPrintConfig = HttpLogUtil.filter(url);
        if (httpPrintConfig.isRequest()) {
            HttpLogUtil.request(url, method.name(), JSON.toJSONString(body, SerializerFeature.DisableCircularReferenceDetect), form, "127.0.0.1", to);
            if(headers != null){
                String RequestId = headers.getFirst("RequestId");
                if(StringUtils.isNotEmpty(RequestId)){
                    log.info("RequestId: {}", RequestId);
                }
            }
        }
        try {
            RestTemplate client = restTemplate;
            HttpHeaders httpHeaders = new HttpHeaders();
            if (headers != null) {
                httpHeaders.addAll(headers);
            }
            // 提交方式：表单、json
            httpHeaders.setContentType(mediaType);
            HttpEntity httpEntity = new HttpEntity<>(body, httpHeaders);
            ResponseEntity<String> response = client.exchange(url, method, httpEntity, String.class);
            String responseBody = response.getBody();
            if (httpPrintConfig.isResponse()) {
                HttpLogUtil.response(response.getStatusCode().value(), responseBody);
            }
            return response;
        } catch (HttpClientErrorException e) {
            String message = e.getResponseBodyAsString();
            if (!httpPrintConfig.isRequest()) {
                HttpLogUtil.request(url, method.name(), JSON.toJSONString(body, SerializerFeature.DisableCircularReferenceDetect), form, "127.0.0.1", to);
            }
            HttpLogUtil.response(e.getStatusCode().value(), message);
            //替换错误信息 得到原始body
            String preface = e.getStatusCode().value() + " " + e.getStatusText() + ": [";
            message = StringUtils.removeStart(message, preface);
            message = StringUtils.removeEnd(message, "]");
            throw new MyHttpException(e.getStatusCode().value(), message);
        } catch (Exception e) {
            log.error("http exception:{}",e.getMessage());
            if (!httpPrintConfig.isRequest()) {
                HttpLogUtil.request(url, method.name(), JSON.toJSONString(body, SerializerFeature.DisableCircularReferenceDetect), form, "127.0.0.1", to);
            }
            HttpLogUtil.response(500, e.getMessage());
            throw new MyHttpException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
        }
    }
}
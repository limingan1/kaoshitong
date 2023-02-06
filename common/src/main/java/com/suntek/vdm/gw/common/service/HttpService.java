package com.suntek.vdm.gw.common.service;


import com.suntek.vdm.gw.common.customexception.MyHttpException;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

public interface HttpService {
    ResponseEntity<String> get(String url, Object body) throws MyHttpException;
    ResponseEntity<String> get(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException;
    ResponseEntity<String> post(String url, Object body) throws MyHttpException;
    ResponseEntity<String> post(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException;
    ResponseEntity<String> put(String url, Object body) throws MyHttpException;
    ResponseEntity<String> put(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException;
    ResponseEntity<String> patch(String url, Object body) throws MyHttpException;
    ResponseEntity<String> patch (String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException;
    ResponseEntity<String> delete(String url, Object body) throws MyHttpException;
    ResponseEntity<String> delete(String url, Object body, MultiValueMap<String, String> headers) throws MyHttpException;
    ResponseEntity<String> request(String url, Object params, MultiValueMap<String, String> headers, HttpMethod method) throws MyHttpException;
}

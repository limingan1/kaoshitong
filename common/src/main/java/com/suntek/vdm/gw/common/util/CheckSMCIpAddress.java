package com.suntek.vdm.gw.common.util;

import com.suntek.vdm.gw.common.pojo.SMCDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class CheckSMCIpAddress {

    public static SMCDto checkSMCIpAddress(SMCDto smcDto){
        int statusCode = 500;
        smcDto.setCode(statusCode);
        InetAddress[] serverIP;
        try {
            serverIP = AnalyticDnsUtils.getServerIP(smcDto.getDomain());
        } catch (Exception e) {
            log.error(e.getLocalizedMessage());
            return smcDto;
        }
        if(serverIP == null || serverIP.length<2){
            return smcDto;
        }
        log.info("===first serverIP===={}",serverIP[0].getHostAddress());
        log.info("===second serverIP===={}",serverIP[1].getHostAddress());
        smcDto.setAddress(serverIP[0].getHostAddress());
        statusCode = testIpAddress(smcDto);
        if(statusCode != 401){//尝试第二个ip地址
            smcDto.setAddress(serverIP[1].getHostAddress());
            statusCode = testIpAddress(smcDto);
        }
        log.info("===statusCode===={}",statusCode);
        smcDto.setCode(statusCode);
        return smcDto;
    }

    public static int testIpAddress(SMCDto smcDto) {
        String url = getUrl(smcDto);
        Map<String, Object> heard = new HashMap<>();
        heard.put("token", 1235412354);
        int statusCode = 500;
        try {
            log.info("==========testIpAddress======{}",url);
            HttpResponse res = httpGet(url, "", "GET", heard, new HashMap<String, Object>());
            if (res != null && res.getStatusLine() != null ){
                statusCode = res.getStatusLine().getStatusCode();
            }
        } catch (Exception e) {
            log.info("testSMCIp error, {}", e);
        }
        return statusCode;
    }

    private static String getUrl(SMCDto smcDto) {
        String address = smcDto.getAddress();
        String[] addresss = address.split(":");
        if (address.indexOf('[') == -1) {
            if (addresss.length > 2) {
                address = "[" + address + "]";
            }
            else if (addresss.length <= 2 && address.length() > 0) {
                address = addresss[0];
            }
        }
        else {
            if (addresss.length == 2) {
                if (addresss[0].length() > 1) {
                    address = addresss[0].substring(1);
                }
            }
            else if (addresss.length == 1) {
                if (addresss[0].length() > 1) {
                    address = addresss[0].substring(1);
                }
            }
        }
        if (address.indexOf('%') != -1) {
            address = address.substring(0, address.indexOf('%'));
            if (address.indexOf('[') != -1) {
                address = address + ']';
            }
        }
        String url = "";
         url = smcDto.getProtocol()
                 + "://"
                 + address
                 + "/conf-portal/organizations";
         log.info("SMC 3.0 url: {}", url);
        return url;
    }


    public static HttpResponse httpGet(String host, String path, String method,
                                       Map<String, Object> headers,
                                       Map<String, Object> querys)
            throws Exception {
        HttpClient httpClient = wrapClient(host);
        RequestConfig config = RequestConfig.custom().setConnectTimeout(5000)    //设置连接超时时间
                .setSocketTimeout(5000).build();   //设置响应超时时间
        HttpGet request = new HttpGet(buildUrl(host, path, querys));
        request.setConfig(config);
        for (Map.Entry<String, Object> e : headers.entrySet()) {
            request.addHeader(e.getKey(), e.getValue().toString());
        }

        return httpClient.execute(request);
    }

    private static String buildUrl(String host, String path, Map<String, Object> querys) throws UnsupportedEncodingException {
        StringBuilder sbUrl = new StringBuilder();
        sbUrl.append(host);
        if (!StringUtils.isEmpty(path)) {
            sbUrl.append(path);
        }
        if (null != querys) {
            StringBuilder sbQuery = new StringBuilder();
            for (Map.Entry<String, Object> query : querys.entrySet()) {
                if (0 < sbQuery.length()) {
                    sbQuery.append("&");
                }
                if (StringUtils.isEmpty(query.getKey()) && !StringUtils.isEmpty(query.getValue().toString())) {
                    sbQuery.append(query.getValue());
                }
                if (!StringUtils.isEmpty(query.getKey())) {
                    sbQuery.append(query.getKey());
                    if (!StringUtils.isEmpty(query.getValue().toString())) {
                        sbQuery.append("=");
                        sbQuery.append(URLEncoder.encode(query.getValue().toString(), "utf-8"));
                    }
                }
            }
            if (0 < sbQuery.length()) {
                sbUrl.append("?").append(sbQuery);
            }
        }

        return sbUrl.toString();
    }


    private static HttpClient wrapClient(String host) {
        HttpClient httpClient = new DefaultHttpClient();
        if (host.startsWith("https://")) {
            sslClient(httpClient);
        }

        return httpClient;
    }


    private static void sslClient(HttpClient httpClient) {
        try {
            SSLContext ctx = SSLContext.getInstance("TLS");
            X509TrustManager tm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                @Override
                public void checkClientTrusted(X509Certificate[] xcs, String str) {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] xcs, String str) {

                }
            };
            ctx.init(null, new TrustManager[]{tm}, null);
            SSLSocketFactory ssf = new SSLSocketFactory(ctx);
            ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            ClientConnectionManager ccm = httpClient.getConnectionManager();
            SchemeRegistry registry = ccm.getSchemeRegistry();
            registry.register(new Scheme("https", 443, ssf));
        } catch (KeyManagementException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

}

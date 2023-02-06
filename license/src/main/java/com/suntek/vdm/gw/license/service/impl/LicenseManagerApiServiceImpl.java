package com.suntek.vdm.gw.license.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.huawei.vdmserver.common.dto.response.GlobalRestResponse;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.license.pojo.LicenseBaseResponse;
import com.suntek.vdm.gw.license.service.LicenseManagerApiService;
import com.suntek.vdm.gw.license.service.WindowLicenseService;
import com.suntek.vdm.gw.license.utills.CommonInputStreamResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

@Service
public class LicenseManagerApiServiceImpl implements LicenseManagerApiService {
    @Autowired
    @Qualifier("licenseHttpServiceImpl")
    private HttpService httpService;

    @Autowired
    private RestTemplate restTemplate;
    @Value("${useAdapt}")
    private Boolean useAdapt;
    @Autowired
    @Lazy
    private WindowLicenseService windowLicenseService;

    /**
     * 申请license资源
     *
     * @param resourceName 资源名称
     * @return
     * @throws MyHttpException
     */
    public LicenseBaseResponse<Integer> allocResource(String resourceName) throws MyHttpException {
        String response = httpService.get("/allocResource/" + resourceName, null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<Integer>>() {
        });
    }

    /**
     * 释放license资源
     *
     * @param resourceName 资源名
     * @return
     * @throws MyHttpException
     */
    public LicenseBaseResponse<String> deallocResource(String resourceName) throws MyHttpException {
        String response = httpService.get("/deallocResource/" + resourceName, null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> getAllResource() throws MyHttpException {
        String response;
        if (useAdapt) {
            GlobalRestResponse<?> oneResource = windowLicenseService.getAllResource();
            LicenseBaseResponse<String> res = new LicenseBaseResponse<>();
            res.setCode(oneResource.getCode());
            Object data = oneResource.getData();
            res.setData(data instanceof String ? (String) data : JSON.toJSONString(data));
            res.setMsg(oneResource.getMsg());
            return res;
        } else {
            response = httpService.get("/getAllResource", null, null).getBody();
        }
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> getRemainingDay() throws MyHttpException {
        String response = httpService.get("/getRemainingDay", null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> getExpiredDate() throws MyHttpException {
        String response = httpService.get("/getExpiredDate", null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> getVdcLicenseStatus() throws MyHttpException {
        String response = httpService.get("/getVdcLicenseStatus", null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> isResourceAvailable(String resourceName) throws MyHttpException {
        String response = httpService.get("/isResourceAvailable/" + resourceName, null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> importLicense(MultipartFile file, boolean isDual) throws MyHttpException {

        // 构建请求头
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.add("appCode", "my-iot-platform-device");
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
        // 构建请求体
        MultiValueMap<String, Object> requestBody = new LinkedMultiValueMap<>();
        CommonInputStreamResource commonInputStreamResource = null;
        try {
            commonInputStreamResource = new CommonInputStreamResource(file.getInputStream(),file.getSize(),file.getOriginalFilename());
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestBody.add("file", commonInputStreamResource);
        requestBody.add("isDual", isDual);

        // 发送上传请求
        HttpEntity<MultiValueMap> requestEntity = new HttpEntity<MultiValueMap>(requestBody, requestHeaders);
        String response = String.valueOf(restTemplate.postForEntity("http://127.0.0.1:9001/license/importLicense",
                requestEntity, LicenseBaseResponse.class).getBody());
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> getEsnCode() throws MyHttpException {
        String response = httpService.get("/getEsnCode", null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> resetResource() throws MyHttpException {
        String response = httpService.get("/resetResource", null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> revoke() throws MyHttpException {
        String response = httpService.get("/revoke", null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    @Override
    public LicenseBaseResponse<?> getRvkTicketAndRvkTime() throws MyHttpException{
        String response = httpService.get("/getRvkTicketAndRvkTime", null, null).getBody();
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    /**
     * 获取单个资源
     *
     * @param category 资源类别
     * @return
     * @throws MyHttpException
     */
    public LicenseBaseResponse<String> getOneResource(String category) throws MyHttpException {
        String response;
        if (useAdapt) {
            GlobalRestResponse<?> oneResource = windowLicenseService.getOneResource(category);
            LicenseBaseResponse<String> res = new LicenseBaseResponse<>();
            res.setCode(oneResource.getCode());
            Object data = oneResource.getData();
            res.setData(data instanceof String ? (String) data : JSON.toJSONString(data));
            res.setMsg(oneResource.getMsg());
            return res;
        } else {
            response = httpService.get("/getOneResource?category=" + category, null, null).getBody();
        }
        return JSON.parseObject(response, new TypeReference<LicenseBaseResponse<?>>() {
        });
    }

    public static File multipartFileToFile(MultipartFile file) throws Exception {

        File toFile = null;
        if (file.equals("") || file.getSize() <= 0) {
            file = null;
        } else {
            InputStream ins = null;
            ins = file.getInputStream();
            toFile = new File(file.getOriginalFilename());
            inputStreamToFile(ins, toFile);
            ins.close();
        }
        return toFile;
    }

    //获取流文件
    private static void inputStreamToFile(InputStream ins, File file) {
        try {
            OutputStream os = new FileOutputStream(file);
            int bytesRead = 0;
            byte[] buffer = new byte[8192];
            while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.close();
            ins.close();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                ins.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

package com.suntek.vdm.gw.license.service;

import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.license.pojo.LicenseBaseResponse;
import org.springframework.web.multipart.MultipartFile;

public interface LicenseManagerApiService {
     LicenseBaseResponse<String> getOneResource(String category) throws MyHttpException;

     LicenseBaseResponse<?> allocResource(String resourceName) throws MyHttpException;

     LicenseBaseResponse<?> deallocResource(String resourceName) throws MyHttpException;

     LicenseBaseResponse<?> getAllResource() throws MyHttpException;

     LicenseBaseResponse<?> getRemainingDay() throws MyHttpException;

     LicenseBaseResponse<?> getExpiredDate() throws MyHttpException;

     LicenseBaseResponse<?> getVdcLicenseStatus() throws MyHttpException;

     LicenseBaseResponse<?> isResourceAvailable(String resourceName) throws MyHttpException;

     LicenseBaseResponse<?> importLicense(MultipartFile file, boolean isDual) throws MyHttpException;

     LicenseBaseResponse<?> getEsnCode() throws MyHttpException;

     LicenseBaseResponse<?> resetResource() throws MyHttpException;

     LicenseBaseResponse<?> revoke() throws MyHttpException;

     LicenseBaseResponse<?> getRvkTicketAndRvkTime() throws MyHttpException;
}

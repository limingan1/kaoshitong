package com.suntek.vdm.gw.license.service;

import com.huawei.vdmserver.common.dto.response.GlobalRestResponse;
import org.springframework.web.multipart.MultipartFile;

public interface WindowLicenseService {
    GlobalRestResponse<?> getEsnCode();

    GlobalRestResponse<?> importLicenseString(MultipartFile file, boolean isDual);

    GlobalRestResponse<?> allocResource(String resourceName);

    GlobalRestResponse<?> deallocResource(String resourceName);

    GlobalRestResponse<?> getAllResource();

    GlobalRestResponse<?> getOneResource(String category);

    GlobalRestResponse<?> getRemainingDay();

    GlobalRestResponse<?> getExpiredDate();

    GlobalRestResponse<?> getVdcLicenseStatus();

    GlobalRestResponse<?>  revokeLicense();

    GlobalRestResponse<?> getRvkTicketAndRvkTime();
}

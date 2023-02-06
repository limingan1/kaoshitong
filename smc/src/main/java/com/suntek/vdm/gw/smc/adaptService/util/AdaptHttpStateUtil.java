package com.suntek.vdm.gw.smc.adaptService.util;

import com.alibaba.fastjson.JSON;
import com.huawei.vdmserver.common.dto.ResponseEntityEx;
import com.suntek.vdm.gw.common.customexception.MyHttpException;

public class AdaptHttpStateUtil {
    public static String dealAdaptHttpStatus(ResponseEntityEx<?> responseEntityEx) throws MyHttpException {
        switch (responseEntityEx.getStatusCodeValue()) {
            case 200:
            case 201:
            case 204:
                return JSON.toJSONString(responseEntityEx.getBody());
            case 401:
                throw new MyHttpException(401, "Unauthorized");
            case 409:
                throw new MyHttpException(409, JSON.toJSONString(responseEntityEx.getBody()));
            case 500:
                throw new MyHttpException(500, JSON.toJSONString(responseEntityEx.getBody()));
            case 502:
                throw new MyHttpException(502, JSON.toJSONString(responseEntityEx.getBody()));
            default:
                throw new MyHttpException(500, JSON.toJSONString(responseEntityEx.getBody()));
        }
    }
}

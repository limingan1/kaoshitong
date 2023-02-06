package com.suntek.vdm.gw.api.handler;

import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.util.HttpLogUtil;
import com.suntek.vdm.gw.conf.service.RedisService;
import com.suntek.vdm.gw.core.annotation.InternInterface;
import com.suntek.vdm.gw.core.annotation.PassLicense;
import com.suntek.vdm.gw.core.annotation.PassToken;
import com.suntek.vdm.gw.core.annotation.SystemConfig;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.common.enums.GwErrorCode;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.license.service.LicenseCheckService;
import com.suntek.vdm.gw.welink.pojo.WelinkNodeData;
import com.suntek.vdm.gw.welink.service.impl.WelinkMeetingManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.servlet.error.BasicErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class AuthenticationInterceptor implements HandlerInterceptor {
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private RedisService redisService;
    @Autowired
    private RoutingDelegateService routingDelegateService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private VmNodeTokenManagerService vmNodeTokenManagerService;
    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private LicenseCheckService licenseCheckService;
    @Value("${localOrgNodeDisplay}")
    private boolean localOrgNodeDisplay;
    @Autowired
    private WelinkMeetingManagerService welinkMeetingManagerService;

    @Value("${cas.database.db_type}")
    private String dbType;

    @Override
    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {
        String realIP = httpServletRequest.getHeader("X-Real-IP");
        String url = httpServletRequest.getRequestURL().toString();
        if (realIP == null) {
            realIP = httpServletRequest.getRemoteAddr();
        }
        String from = httpServletRequest.getHeader("gw-http-code") == null ? "GW" : "C";
        String requestMethod = httpServletRequest.getMethod();
        // 如果不是映射到方法直接通过
        if (!(object instanceof HandlerMethod)) {
            return true;
        }
        //找不到的请求不拦截
        if (((HandlerMethod) object).getBeanType().equals(BasicErrorController.class)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) object;
        Method method = handlerMethod.getMethod();
        if (method.isAnnotationPresent(InternInterface.class)) {
            if ("127.0.0.1".equals(realIP) || "localhost".equals(realIP)) {
                return true;
            }
        }
        if (!licenseCheckService.hasLicense()) {  //判断请求是否有license
            if (!method.isAnnotationPresent(PassLicense.class)) {
                HttpLogUtil.request(url, requestMethod, "请检查license", from, realIP, "S");
                returnRes(httpServletResponse,409,GwErrorCode.NO_LICENSE_ERROR.toString());
                return false;
            }
        }

        //检查是否有passtoken注释，有则跳过认证
        if (method.isAnnotationPresent(PassToken.class)) {
            PassToken passToken = method.getAnnotation(PassToken.class);
            if (passToken.required()) {
                return true;
            }
        }
        String token = httpServletRequest.getHeader("Token");// 从 http 请求头中取出 token
        if (token == null || token.equals(CoreConfig.INTERNAL_USER_TOKEN)) {
            httpServletResponse.setStatus(401);
            HttpLogUtil.request(url, requestMethod, "Unauthorized", from, realIP, "S");
            return false;
        }
        if (!localTokenManageService.expired(token) || !vmNodeTokenManagerService.expired(token)) {
//            try {
//                localTokenManageService.keepAlive(token);
//            } catch (BaseStateException e) {
//                httpServletResponse.setStatus(401);
//                return false;
//            }
            String casOrgId = httpServletRequest.getParameter("casOrgId");
            String gwIdStr = httpServletRequest.getHeader("TargetId");// 从 http 请求头中取出 token
            if (StringUtils.isEmpty(gwIdStr)) {
                gwIdStr = casOrgId;
            }
            if (!StringUtils.isEmpty(gwIdStr)) {
                GwId gwId = new GwId(gwIdStr);
                if(!localOrgNodeDisplay){
                    LocalToken localToken = localTokenManageService.get(token);
                    if (localToken == null) {
                        return true;
                    }
                    GwId tokenGwId = localToken.getGwId();
                    log.info("tokenGwId:{}", localToken);
                    log.info("gwId:{}", gwId);
                    if (tokenGwId == null) {
                        WelinkNodeData welinkNodeData = welinkMeetingManagerService.getWelinkNodeData();
                        if (welinkNodeData == null || !welinkNodeData.getId().equals(gwId.getNodeId())) { //这里排除掉发往welink的请求
                            return true;
                        }
                    }
                    if (casOrgId != null && !"".equals(casOrgId)) {
                        GwId casOrgGwId = new GwId(casOrgId);
                        if (gwId.equals(tokenGwId) || casOrgGwId.equals(tokenGwId)) {
                            return true;
                        }
                    }
                }

                String pureUrl = httpServletRequest.getRequestURI();
                if (permissionService.checkPermissionUrl(pureUrl,gwId)) {
                    if (!routManageService.isLocalVm(token) && routManageService.isLocal(gwId)) {
                        return true;
                    }
                    boolean localVm = "true".equals(httpServletRequest.getHeader("localVm"));
                    VmNodeData vmNode = vmNodeDataService.getOneByToken(token);
                    if (localVm || (vmNode != null && gwId.getNodeId().equals(vmNode.getId()))) {
                        return true;
                    }
                }else{
                    //其他接口如果是本级，直接返回true
                    if(routManageService.isLocal(gwId)){
                        return true;
                    }
                }
                ResponseEntity response = routingDelegateService.redirect(gwId, httpServletRequest);
                httpServletResponse.setStatus(response.getStatusCodeValue());
                httpServletResponse.setContentType("application/json; charset=utf-8");
                PrintWriter writer = httpServletResponse.getWriter();
                writer.print(response.getBody());
                writer.close();
                httpServletResponse.flushBuffer();
                String body = null;
                if(response.getBody() != null){
                    Objects.requireNonNull(response.getBody()).toString();
                }
                HttpLogUtil.request(url, requestMethod, body, from, realIP, "S");
                return false;
            }
            return true;
        } else if (method.isAnnotationPresent(SystemConfig.class)) {
                 //系统配置鉴权使用redis
//        if (method.isAnnotationPresent(SystemConfig.class) && use_redis) {
            SystemConfig systemConfig = method.getAnnotation(SystemConfig.class);
            if (systemConfig.required()) {
                //FIXME 此处Token没有保活
                if (!redisService.hasKey(token)) {
                    httpServletResponse.setStatus(401);
                    HttpLogUtil.request(url, requestMethod, "Unauthorized", from, realIP, "S");
                    return false;
                }
                return true;
            }
        }else {
            httpServletResponse.setStatus(401);
            HttpLogUtil.request(url, requestMethod, "Unauthorized", from, realIP, "S");
            return false;
        }
        return true;
    }



    private void returnRes(HttpServletResponse httpServletResponse,int status,String body) throws IOException {
        HttpLogUtil.response(status, body);
        //如果是本级是虚拟节点且没有开启权限
        httpServletResponse.setStatus(status);
        httpServletResponse.setContentType("application/json; charset=utf-8");
        PrintWriter writer = httpServletResponse.getWriter();
        writer.print(body);
        writer.close();
        httpServletResponse.flushBuffer();
    }


    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse,
                                Object o, Exception e) throws Exception {
    }
}

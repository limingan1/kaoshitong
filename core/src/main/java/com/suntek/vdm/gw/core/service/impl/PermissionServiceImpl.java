package com.suntek.vdm.gw.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.entity.OrgUserData;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.pojo.RemoteToken;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.core.service.orgUser.OrgUserDataService;
import com.suntek.vdm.gw.core.service.orgUser.OrgUserTokenManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private OrgUserDataService orgUserDataService;
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private OrgUserTokenManagerService orgUserTokenManagerService;
    @Value("${localOrgNodeDisplay}")
    private boolean localOrgNodeDisplay;
    /**
     * 校验普通节点权限（在发送请求时校验） true: 放行  false: 拦截
     */
    @Override
    public boolean checkPermission(GwId targetGwId, String token,MultiValueMap<String,String> headers) throws MyHttpException {
        String pureUrl = request.getRequestURI();
        if (checkPermissionUrl(pureUrl, null)) {
            VmNodeData vmNode = vmNodeDataService.getOneByToken(token);
            NodeData local = nodeDataService.getLocal();
            GwNode realLocalId = vmNode != null ? vmNode.toGwNode() : local.toGwNode();
            String targetNodeId = targetGwId.getNodeId();
            GwNode realLocalGwId = nodeManageService.getByNodeId(realLocalId.getId());
            GwNode child = realLocalGwId.getChildByNodeId(targetNodeId);
            if (targetNodeId.equals(local.getId()) || (vmNode != null && targetNodeId.equals(vmNode.getId())) || child != null) {
                log.info("add local vm true 11");
                headers.add("localVm", "true");
            } else {
                headers.remove("localVm");
            }
            boolean hasPermission = false;
            log.info("realLocalId1:{}", realLocalId.getName() + "," + realLocalId.getId() + "," + realLocalId.getPermissionSwitch());
            log.info("targetGwId1:{}", targetGwId);
            log.info("child info1:{}", child);
            if (targetNodeId.equals(realLocalId.getId()) || child != null) {
                //请求发送给下级
                hasPermission = true;
            } else {
                GwNode nextNode;//当前节点的下一个上级节点
                String currentId = realLocalId.getId();
                GwNode currentNode = realLocalId;//当前节点
                GwNode commonParentNode = nodeManageService.getCommonParentNode(targetNodeId, currentId);
                if (commonParentNode == null) {
                    log.error("organization tree error,not fond");
                    //暂时放行1
                    return true;
                }
                if (!localOrgNodeDisplay && realLocalGwId.isVm() && StringUtils.isBlank(realLocalGwId.getParentId()) && targetNodeId.equals(local.getId())) {
                    log.info("localOrgNodeDisplay:{},local vm gwNode:{}", localOrgNodeDisplay, realLocalGwId);
                    return true;
                }
                //遍历到目标节点和源节点最近的公共父节点
                log.info("commonParentNode:{},id:{}", commonParentNode.getName(),commonParentNode.getId());
                while ((nextNode = nodeManageService.getNextUpperNode(currentId, true)) != null) {
                    log.info("nextNode:{},id:{},areaCode:{}", nextNode.getName(), nextNode.getId(), nextNode.getAreaCode());
                    hasPermission = !new Integer(0).equals(currentNode.getPermissionSwitch());
                    if (commonParentNode.getId().equals(nextNode.getId())) {
                        break;
                    }
//                        hasPermission = !new Integer(0).equals(currentNode.getPermissionSwitch());
                    if (!hasPermission) {
                        break;
                    }
                    currentId = nextNode.getId();
                    currentNode = nextNode;
                    hasPermission = false;
                }
            }
            if (hasPermission) {
                checkOrgUser(targetGwId, request, headers);
                if (realLocalId.isVm() && targetGwId.getNodeId().equals(local.getId())) {
                    headers.remove("localVm");
                    headers.add("localVm", "true");
                }
            }
            return hasPermission;
        }
        //不属于上述url则无需校验
//        checkOrgUser(targetGwId, request, headers);
        return true;
    }

    private void checkOrgUser(GwId id, HttpServletRequest request, MultiValueMap<String, String> headers) throws MyHttpException {
        NodeData localNodeData = nodeDataService.getLocal();
        //获取直接连接节点GwId
        GwId realId = null;
        boolean isLocal = false;
        if(localNodeData.toGwId().equals(id)){
            realId = id;
            isLocal = true;
        }else {
            realId = routManageService.getWayByGwId(id);
        }
        if (realId == null) {
            NodeData topNodeData = nodeDataService.getTop();
            if (topNodeData == null) {
                throw new MyHttpException(HttpStatus.NOT_ACCEPTABLE.value(), "smc node not found.");
            } else {
                realId = topNodeData.toGwId();
            }
        }
        if(!isLocal){
            String token = request.getHeader("Token");
            LocalToken localToken = localTokenManageService.get(token);
            if(localToken == null){
                throw new MyHttpException(HttpStatus.UNAUTHORIZED.value(), "Unauthorized.");
            }
            OrgUserData orgUserData = orgUserDataService.getOneByLambda(new LambdaQueryWrapper<OrgUserData>().eq(OrgUserData::getNodeId, realId.getNodeId()).eq(OrgUserData::getOrgId,localToken.getOrgId()));
            if(orgUserData != null){
                RemoteToken remoteToken = orgUserTokenManagerService.get(orgUserData.getId());
                if(remoteToken == null){
                    orgUserTokenManagerService.triggerKeepAlive(orgUserData);
                    remoteToken = orgUserTokenManagerService.get(orgUserData.getId());
                }
                headers.remove("Token");
                headers.add("Token", remoteToken == null ? null : remoteToken.getToken());
                headers.add("useOrgUser","true");
            }else{
                headers.remove("useOrgUser");
            }
        }
    }

    @Override
    public boolean checkPermissionUrl(String pureUrl, GwId gwId) {
        if (gwId != null) {
            NodeBusinessType nodeBusinessType = nodeManageService.getNodeBusinessType(gwId.getNodeId());
            if(NodeBusinessType.isWelinkOrCloudLink(nodeBusinessType)){
                return false;
            }
        }
        for (String regex : urlRegex) {
            if (pureUrl.matches(regex)) {
                return true;
            }
        }
        return false;
//        switch (pureUrl) {
//            case "/conf-portal/addressbook/rooms/conditions":
//            case "/conf-portal/addressbook/rooms":
//            case "/conf-portal/conferences/conditions":
//            case "/conf-portal/addressbook/users/conditions":
//            case "/conf-portal/addressbook/organizations":
//                return true;
//            default:
//                return false;
//        }
    }

    private static final String[] urlRegex = {
            "/conf-portal/addressbook/rooms/conditions",
            "/conf-portal/addressbook/rooms",
            "/conf-portal/conferences/conditions",
            "/conf-portal/addressbook/users/conditions",
            "/conf-portal/addressbook/organizations",
            "/conf-portal/addressbook/organizations/([^/ ]+)"
    };
}

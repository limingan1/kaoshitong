package com.suntek.vdm.gw.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.WarningReportDto;
import com.suntek.vdm.gw.common.pojo.WarningType;
import com.suntek.vdm.gw.common.pojo.node.NodeStatusMsg;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.service.WarningReportService;
import com.suntek.vdm.gw.common.util.AuthorizationUtil;
import com.suntek.vdm.gw.common.util.Encryption;
import com.suntek.vdm.gw.core.api.response.node.GetNodeTokenResponse;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.enums.CoreApiUrl;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.common.enums.NodeStatusType;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.common.pojo.node.NodeStatus;
import com.suntek.vdm.gw.core.pojo.NodeStatusChange;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import com.suntek.vdm.gw.smc.service.SmcLoginService;
import com.suntek.vdm.gw.welink.api.response.AuthResponse;
import com.suntek.vdm.gw.welink.pojo.WelinkNodeData;
import com.suntek.vdm.gw.welink.service.WeLinkLinkService;
import com.suntek.vdm.gw.welink.service.WelinkAddressBookService;
import com.suntek.vdm.gw.welink.service.impl.WelinkMeetingManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class NodeManageServiceImpl implements NodeManageService {

    @Value("${server.port}")
    private Integer httpsPort;

    @Autowired
    private NodeLinkService nodeLinkService;
    @Autowired
    private RemoteTokenManageService remoteTokenManageService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private NodePushService nodePushService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private WeLinkLinkService weLinkLinkService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private WelinkMeetingManagerService welinkMeetingManagerService;

    @Autowired
    private LocalTokenManageService localTokenManageService;

    @Autowired
    private WarningReportService warningReportService;
    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private SmcLoginService smcLoginService;
    @Autowired
    private VmNodeTokenManagerService vmNodeTokenManagerService;
    @Autowired
    private WelinkAddressBookService welinkAddressBookService;

    @Autowired
    @Qualifier("httpServiceImpl")
    private HttpService httpService;

    private static Map<String, NodeStatus> nodeStatusMap = new ConcurrentHashMap<>();

    private static Queue<NodeStatusChange> nodeStatusChangeMap = new LinkedList<>();

    private static Map<String, NodeStatusMsg> nodeLoginMsgMap = new ConcurrentHashMap<>();

    /**
     * 来自下级推送的组织树
     */
    private static Map<String, GwNode> nodeFromChild = new ConcurrentHashMap<>();

    private static GwNode organizationNode;


    private static Map<String, GwNode> organizationNodeMap = new ConcurrentHashMap<>();


    private static Interner<String> pool = Interners.newWeakInterner();

    @Override
    public String getLoginMsg(String id){
        NodeStatusMsg nodeStatusMsg = nodeLoginMsgMap.get(id);
        if(nodeStatusMsg == null){
            return null;
        }
        return nodeStatusMsg.getLoginMsg();
    }

    public void setLoginMsg(String id, String msg){
        NodeStatusMsg nodeStatusMsg = nodeLoginMsgMap.get(id);
        if(nodeStatusMsg == null){
            nodeStatusMsg = new NodeStatusMsg();
            nodeLoginMsgMap.put(id, nodeStatusMsg);
        }
        String oldMsg = nodeStatusMsg.getLoginMsg();
        nodeStatusMsg.setLoginMsg(msg);
        if(msg != null && !msg.equals(oldMsg)){
            autoPush();
        }
        if(!StringUtils.isEmpty(msg)){
            NodeData node = nodeDataService.getOneById(id);
            dealWarningMsg(node, msg);
        }
    }
    @Override
    public void setRemoteLoginMsg(String id, String msg){
        NodeStatusMsg nodeStatusMsg = nodeLoginMsgMap.get(id);
        if(nodeStatusMsg == null){
            nodeStatusMsg = new NodeStatusMsg();
            nodeLoginMsgMap.put(id, nodeStatusMsg);
        }
        String oldRemoteMsg = nodeStatusMsg.getRemoteLoginMsg();
        nodeStatusMsg.setRemoteLoginMsg(msg);
        if(msg != null && !msg.equals(oldRemoteMsg)){
            NodeData node = nodeDataService.getOneById(id);
            if(node != null && node.getType().equals(NodeType.TOP.value()) && !StringUtils.isEmpty(msg)){
                GwNode gwNode = getFullLocalGwNode();
                setOrganizationNode(gwNode);
                nodePushService.sendLowTree(gwNode);
            }else {
                autoPush();
            }

            if(!StringUtils.isEmpty(msg)){
                dealWarningMsg(node, msg);
            }
        }
    }

    private void dealWarningMsg(NodeData node, String msg){
        WarningReportDto warningReportDto = new WarningReportDto();
        warningReportDto.setOmcSource("B");

        warningReportDto.setOmcLevel(0);
        warningReportDto.setOmcType(4);
        switch(msg){
            case "Remote node not configured":
                warningReportDto.setOmcName("10000001;"+node.getName());//请检查远端节点%s是否配置本节点信息
                warningReportDto.setWarningType(WarningType.REMOTE_NODE_NOT_CONFIG);
                break;
            case "node login error.":
                warningReportDto.setOmcName("10000002;"+node.getName());//节点%s登录异常，请检查账号信息
                warningReportDto.setWarningType(WarningType.NODE_LOGIN_ERROR);
                break;
            case "http connect error.":
                warningReportDto.setOmcName("10000003;"+node.getName());//节点%s网络连接异常，请检查网络状态
                warningReportDto.setWarningType(WarningType.HTTP_CONNECT_ERROR);
                break;
            case "remote server error.":
                warningReportDto.setOmcName("10000004;"+node.getName());//节点%s服务异常，请检查远端服务状态
                warningReportDto.setWarningType(WarningType.REMOTE_SERVER_ERROR);
                break;
        }
        warningReportService.addWarningReport(warningReportDto);
    }


    @Override
    public void setOrganizationNode(GwNode organizationNode) {
        log.info("organizationNode1:{}", organizationNode);
        NodeManageServiceImpl.organizationNode = organizationNode;
        routManageService.generateTrie(organizationNode);
    }

    @Override
    public GwNode getOrganizationNode() {
        return organizationNode;
    }

    @Override
    public void add(String id) {
        NodeData nodeData = nodeDataService.getOneById(id);
        NodeType nodeType = NodeType.valueOf(nodeData.getType());
        log.info("Load node id:{} name:{} code:{} type:{} to localCache", nodeData.getId(), nodeData.getName(), nodeData.getAreaCode(), nodeData.getType());
        if (nodeType.equals(NodeType.THIS)) {
            log.info("Load local smc address success");
        } else {
            String nodeId = null;
            try {
                nodeId = loginNode(nodeData);
            } catch (MyHttpException e) {
                e.printStackTrace();
            }
            if (nodeId != null) {
                log.info("Login remote node id:{} name:{} code:{} success", nodeData.getId(), nodeData.getName(), nodeData.getAreaCode(), nodeData.getType());
            } else {
                log.info("Login remote node id:{} name:{} code:{} fail", nodeData.getId(), nodeData.getName(), nodeData.getAreaCode(), nodeData.getType());
            }
        }
    }


    @Override
    public void addVmNode(String id) {
        VmNodeData vmNodeData = vmNodeDataService.getOneById(id);
        log.info("Load vm node id:{} name:{} code:{} to localCache", vmNodeData.getId(), vmNodeData.getName(), vmNodeData.getAreaCode());
        try {
            loginVmNode(vmNodeData);
        } catch (MyHttpException exception) {
            log.info("Load vm node id:{} name:{} code:{} failes", vmNodeData.getId(), vmNodeData.getName(), vmNodeData.getAreaCode());
        }
        sendTopTreeVmHandler(vmNodeData.toGwNode());
    }

    public void loginVmNode(VmNodeData vmNodeData) throws MyHttpException {
        synchronized (pool.intern(vmNodeData.getId())) {
            String password = vmNodeData.decryptPassword();
            String authorization = AuthorizationUtil.getAuthorization(vmNodeData.getUsername(), password);
            GetTokenResponse response = smcLoginService.getTokens(authorization);
            String username = Encryption.decryptBase64(authorization.replace("Basic ", "")).split(":")[0];
            vmNodeTokenManagerService.add(vmNodeData.getId(), response.getUuid(), vmNodeData.getAreaCode(), Long.valueOf(response.getExpire()), username);
            vmNodeTokenManagerService.getNodeKeepAliveInfoById(vmNodeData.getId()).success();
        }
    }

    @Override
    public void update(String id) {
        NodeData nodeData = nodeDataService.getOneById(id);
        NodeType nodeType = NodeType.valueOf(nodeData.getType());
        log.info("update node id:{} name:{} code:{} type:{} to localCache", nodeData.getId(), nodeData.getName(), nodeData.getAreaCode(), nodeData.getType());
        if (nodeType.equals(NodeType.THIS)) {
            log.info("update local smc address success");
            nodePushService.noticeRemoteLocalNodeUpdate();
            autoPush();
        } else {
            remoteTokenManageService.del(nodeData.getId());
            try {
                String nodeId = loginNode(nodeData);
            } catch (MyHttpException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void del(String id, String name, NodeType type) {
        nodeStatusMap.remove(id);
        //删除节点了推送组织树
        if (type.equals(NodeType.LOW)) {
            nodeFromChild.remove(id);
            routManageService.cleanRoute(id);
        }
        nodeLoginMsgMap.remove(id);
        autoPush();
        nodeStatusChangeAfter(id, name, type);
    }

    @Override
    public String loginNode(NodeData nodeData) throws MyHttpException {
        synchronized (pool.intern(nodeData.getId())) {
            boolean isSmc = !(NodeBusinessType.WELINK.value() == nodeData.getBusinessType() || NodeBusinessType.CLOUDLINK.value() == nodeData.getBusinessType());
            try {
                NodeBusinessType nodeBusinessType = nodeData.toNodeBusinessType();
                GetNodeTokenResponse response = null;
                switch (nodeBusinessType) {
                    case SMC: {
                        response = nodeLinkService.loginNode(nodeData.getId(), nodeData.getUsername(), nodeData.decryptPassword(), nodeData.getIp(), nodeData.isHttps(), nodeData.getAreaCode());
                        break;
                    }
                    case WELINK: {
                        WelinkNodeData welinkNodeData = new WelinkNodeData();
                        BeanUtils.copyProperties(nodeData, welinkNodeData);
                        welinkNodeData.setGwId(new GwId(nodeData.getId(), nodeData.getAreaCode()));
                        welinkMeetingManagerService.setWelinkNodeData(welinkNodeData);
                        AuthResponse authResponse = weLinkLinkService.login(nodeData.getIp(), nodeData.getUsername(), nodeData.decryptPassword());
                        response = new GetNodeTokenResponse();
                        response.setId(nodeData.getId());
                        response.setAreaCode(nodeData.getAreaCode());
                        response.setName(nodeData.getName());
                        response.setSmcVersion(nodeData.getSmcVersion());
                        response.setUuid(authResponse.getAccessToken());
                        response.setExpire(authResponse.getExpireTime().toString());
                        welinkAddressBookService.getTickets(nodeData.getClientId(), nodeData.getClientSecret(), nodeData.getAddressBookUrl(), false);
                        break;
                    }
                    case CLOUDLINK: {
                        WelinkNodeData welinkNodeData = new WelinkNodeData();
                        BeanUtils.copyProperties(nodeData, welinkNodeData);
                        welinkNodeData.setGwId(new GwId(nodeData.getId(), nodeData.getAreaCode()));
                        welinkMeetingManagerService.setWelinkNodeData(welinkNodeData);
                        AuthResponse authResponse = weLinkLinkService.cloudLinkLogin(nodeData.getIp(), nodeData.getUsername(), nodeData.decryptPassword());
                        response = new GetNodeTokenResponse();
                        response.setId(nodeData.getId());
                        response.setAreaCode(nodeData.getAreaCode());
                        response.setName(nodeData.getName());
                        response.setSmcVersion(nodeData.getSmcVersion());
                        response.setUuid(authResponse.getAccessToken());
                        response.setExpire(authResponse.getExpireTime().toString());
                        break;
                    }
                    default:
                        return null;
                }
                //说明远端id变化了
                String oldId = nodeData.getId();
                if (!oldId.equals(response.getId())) {
                    //处理数据库
                    log.info("remote node id  update old:{} new:{}", nodeData.getId(), response.getId());
                    nodeDataService.updatePrimaryKey(response.getId(), oldId);
                    nodeData = nodeDataService.getOneById(response.getId());
                    routManageService.cleanRoute(oldId);
                }
                checkNodeInfoChange(response.getName(), response.getAreaCode(), response.getSmcVersion(), nodeData);
                remoteTokenManageService.add(response.getId(), response.getUuid(), nodeData.getIp(), nodeData.isHttps(), nodeData.getAreaCode(), Long.valueOf(response.getExpire()));
                //设置节点呼出成功
                setNodeOutStatus(response.getId(), NodeStatusType.ONLINE);
                //如果登录的是上级成功  发送本地树木
                if (nodeData.getType().equals(NodeType.TOP.value())) {
                    nodePushService.sendTopTree(getFullLocalGwNode());
                }
                //welink组织树
                if(!isSmc){
                    NodeStatus nodeStatus = getNodeStatus(nodeData.getId());
                    // if (nodeStatus.linked()) {
                    GwNode welinkGwNode = nodeFromChild.get(nodeData.getId());
                    if (welinkGwNode == null) {
                        welinkGwNode = nodeData.toGwNode();
                    } else {
                        welinkGwNode.setDisplayDirectlyUnder(nodeData.getDisplayDirectlyUnder());
                    }
                    welinkGwNode.setIp(nodeData.getIp());
                    nodeManageService.sendTopTreeHandler(welinkGwNode);
                }

                try {
                    long smcTime = System.currentTimeMillis() - LocalTokenManageServiceImpl.timeDifference;
                    //重新补发订阅
                    String url = "https://127.0.0.1:" + httpsPort + String.format(CoreApiUrl.CASCADE_RESUME_LOW_SUBSCRIBE.value(), response.getId());
                    MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
                    String randomToken = UUID.randomUUID().toString();
                    localTokenManageService.add(randomToken, randomToken, randomToken, smcTime + 1000 * 10, false, null);
                    headers.add("Token", randomToken);
                    httpService.post(url, null, headers);
                } catch (Exception e) {
                    log.error("resumeLowSubscribe error", e);
                }
                if (!oldId.equals(response.getId())) {
                    remoteTokenManageService.removeNodeKeepAliveInfoById(oldId);
                }
                remoteTokenManageService.getNodeKeepAliveInfoById(response.getId()).success();
                if (isSmc) {
                    setLoginMsg(nodeData.getId(), "");
                }
                return response.getId();
            } catch (MyHttpException e) {
                if(e.getCode() == 401 && isSmc){
                    setLoginMsg(nodeData.getId(), "node login error.");
                }
                if(e.getBody() != null && isSmc){
                    if(e.getBody().contains("No route to host: connect")){
                        setLoginMsg(nodeData.getId(), "http connect error.");
                    }else if (e.getBody().contains("Bad Gateway")){
                        setLoginMsg(nodeData.getId(), "remote server error.");
                    }
                }
                setNodeOutStatus(nodeData.getId(), NodeStatusType.OFFLINE);
                throw e;
            } catch (Exception e) {
                log.error("exception", e);
                setNodeOutStatus(nodeData.getId(), NodeStatusType.OFFLINE);
            }
            //如果登录下级失败 发送组织树到上级
            if (nodeData.isRealNode() && nodeData.getType().equals(NodeType.LOW.value())) {
                GwNode gwNode = getFullLocalGwNode();
                setOrganizationNode(gwNode);
                nodePushService.sendTopTree(gwNode);
            }
            return null;
        }
    }


    /**
     * 处理来自下级推送的节点信息
     *
     * @param gwNode
     * @return
     */
    @Override
    public void sendTopTreeHandler(GwNode gwNode) {
        log.info("Handler form low push tree");
        NodeData lower = nodeDataService.getOneById(gwNode.getId());
        if (lower != null) {
            //加入节点缓存
            nodeFromChild.put(lower.getId(), gwNode);
            //初始化路由
            routManageService.generateRoute(gwNode);
        }
        autoPush();
    }

    @Override
    public void sendTopTreeVmHandler(GwNode gwNode) {
        log.info("Handler form low push tree");
        VmNodeData lower = vmNodeDataService.getOneById(gwNode.getId());
        if (lower != null) {
            //初始化路由
            routManageService.generateRoute(gwNode);
        }
        autoPush();
    }

    /**
     * 检测节点信息变化
     *
     * @param name
     * @param areaCode
     * @param smcVersion
     * @param nodeData
     */
    public void checkNodeInfoChange(String name, String areaCode, String smcVersion, NodeData nodeData) {
        boolean changeFlag = false;
        if (!name.equals(nodeData.getName())) {
            nodeData.setName(name);
            changeFlag = true;
        }
        if (!areaCode.equals(nodeData.getAreaCode())) {
            nodeData.setAreaCode(areaCode);
            changeFlag = true;
        }
        if (!smcVersion.equals(nodeData.getSmcVersion())) {
            nodeData.setSmcVersion(smcVersion);
            changeFlag = true;
        }
        if (changeFlag) {
            nodeData.setPassword(nodeData.decryptPassword());
            nodeDataService.update(nodeData);
        }
    }


    public void autoPush() {
        //判断自己是不是顶级
        NodeData top = nodeDataService.getTop();
        if (top != null) {
            GwNode fullLocalGwNode = getFullLocalGwNode();
            nodePushService.sendTopTree(fullLocalGwNode);
            setOrganizationNode(fullLocalGwNode);
            return;
        }
        GwNode gwNode = getFullLocalGwNode();
        setOrganizationNode(gwNode);
        if (nodeDataService.getLow() != null) {
            nodePushService.sendLowTree(gwNode);
        }

    }

    public GwNode getFullLocalGwNode() {
        NodeData local = nodeDataService.getLocal();
        if(local == null) return null;
        GwNode gwNodePush = local.toGwNode();
        VmNodeData vmTop = vmNodeDataService.getOneByLambda(new LambdaQueryWrapper<VmNodeData>().isNull(VmNodeData::getParentId).or().eq(VmNodeData::getParentId,""));
        if(vmTop != null){
//            组装vm child
            List<VmNodeData> vmList = vmNodeDataService.getAll();
            GwNode vmNode = vmTop.toGwNode();
            getFullVmGwNode(vmList, vmNode);
            vmList = vmNodeDataService.getAll();
            for(VmNodeData vmNodeData: vmList){
                nodeFromChild.put(vmNodeData.getId(), vmNode);
            }

            gwNodePush.getChild().add(vmNode);
        }

        List<NodeData> lows = nodeDataService.getLow();
        for (NodeData item : lows) {
            NodeStatus nodeStatus = getNodeStatus(item.getId());
            NodeStatusMsg nodeStatusMsg = nodeLoginMsgMap.get(item.getId());
            String msg = null;
            if(nodeStatusMsg != null){
                msg = dealLogMsg(nodeStatusMsg);
            }
            GwNode lowGwNode = nodeFromChild.get(item.getId());
            if (lowGwNode == null) {
                lowGwNode = item.toGwNode();
            }
            lowGwNode.setPermissionSwitch(item.getPermissionSwitch());
            lowGwNode.setNodestatus(nodeStatus);
            if(item.getBusinessType() == 2 || item.getBusinessType() == 3){
                nodeStatus.setIn(NodeStatusType.ONLINE);
            }
            lowGwNode.setMsg(msg);
            lowGwNode.setIp(item.getIp());
            gwNodePush.getChild().add(lowGwNode);
            // }
        }
        return gwNodePush;
    }

    public GwNode getFullVmGwNode(List<VmNodeData> vmList, GwNode vmGwNode){
        if(vmList == null || vmList.isEmpty()){
            return vmGwNode;
        }
        Iterator<VmNodeData> it = vmList.iterator();
        while (it.hasNext()){
            VmNodeData vmNodeData = it.next();
            if(vmNodeData.getParentId() == null || !vmNodeData.getParentId().equals(vmGwNode.getId())){
                continue;
            }
            GwNode childGwNode = vmNodeData.toGwNode();
            vmGwNode.getChild().add(childGwNode);
            it.remove();
        }
        if(vmGwNode.getChild().size()>0){
            for(GwNode childGwNode: vmGwNode.getChild()){
                getFullVmGwNode(vmList, childGwNode);
            }
        }
        return vmGwNode;
    }

    public String dealLogMsg(NodeStatusMsg nodeStatusMsg){
        String msg = nodeStatusMsg.getLoginMsg();
        if(!"".equals(msg)){
            return msg;
        }
        String remoteMsg = nodeStatusMsg.getRemoteLoginMsg();
        if(remoteMsg == null){
            return "Remote node not configured";
        }else {
            return remoteMsg;
        }
    }

    /**
     * 处理上级下发的组织树信息
     *
     * @param gwNode
     * @throws BaseStateException
     */
    @Override
    public void sendLowTreeHandler(GwNode gwNode) {
        log.info("Handler form top Issued organizationTree");
        setOrganizationNode(gwNode);
        //初始化路由
        List<NodeData> lows = nodeDataService.getLow();
        if (lows != null) {
            for (NodeData item : lows) {
                try {
                    nodePushService.sendLowTree(item.toGwId(), getOrganizationNode());
                } catch (MyHttpException e) {
                    //TODO 失败的处理逻辑
                }
            }
        } else {
            log.info("Not found low");
        }
    }


    public void nodeStatusChangeAfter(String id, String name, NodeType nodeType) {
        NodeStatus nodeStatus = nodeStatusMap.get(id);
        boolean linked = nodeStatus == null ? false : nodeStatus.linked();
        log.info("node status change after name:{} type:{} linked:{}", name, nodeType.name(), linked);
//        if (smcNodeType.equals(SmcNodeType.LOW)) {
//            if (nodeStatus!=null&&nodeStatus.linked()) {
//                 //等待下级发送
//            } else {
//                autoPush();
//            }
//        } else if (smcNodeType.equals(SmcNodeType.TOP))
//            if (nodeStatus!=null&&nodeStatus.linked()) {
//                nodePushService.sendTopTree(getFullLocalGwNode());
//            } else {
//                GwNode gwNode = getFullLocalGwNode();
//                setOrganizationNode(gwNode);
//                nodePushService.sendLowTree(gwNode);
//            }
    }

    public void setNodeInStatus(String id, NodeStatusType statusType) {
        NodeData nodeData = nodeDataService.getOneById(id);
        if (nodeData == null) {
            nodeStatusMap.remove(id);
        } else {
            log.info("node in status name:{} status:{}", nodeData.getName(), statusType.value());
            if (statusType.equals(NodeStatusType.ONLINE)) {
//                //检查自己有没有连接上节点
//                //没有的话连接
//               if (nodeStatusMap.get(id).out.equals(NodeStatusType.OFFLINE)){
//                   loginNode(nodeData);
//               }
            }
            NodeStatus nodeStatus = nodeStatusMap.get(id);
            if (nodeStatus == null) {
                nodeStatus = new NodeStatus();
                nodeStatusMap.put(id, nodeStatus);
            }
            nodeStatus.setIn(statusType);
            nodeStatusChangeAfter(id, nodeData.getName(), NodeType.valueOf(nodeData.getType()));

        }
    }


    public void setNodeOutStatus(String id, NodeStatusType statusType) {
        NodeData nodeData = nodeDataService.getOneById(id);
        if (nodeData == null) {
            nodeStatusMap.remove(id);
        } else {
            log.info("node out status name:{} status:{}", nodeData.getName(), statusType.value());
            NodeStatus nodeStatus = nodeStatusMap.get(id);
            if (nodeStatus == null) {
                nodeStatus = new NodeStatus();
                nodeStatusMap.put(id, nodeStatus);
            }
            nodeStatus.setOut(statusType);
            nodeStatusChangeAfter(id, nodeData.getName(), NodeType.valueOf(nodeData.getType()));
        }
    }

    public NodeStatus getNodeStatus(String id) {
        NodeStatus nodeStatus = nodeStatusMap.get(id);
        if (nodeStatus == null) {
            nodeStatus = new NodeStatus();
        }
        return nodeStatus;
    }

    public NodeBusinessType getNodeBusinessType(String id){
        GwNode gwNode = nodeFromChild.get(id);
        if(gwNode == null){
            if (nodeDataService.getLocal().getId().equals(id)) {
                return NodeBusinessType.SMC;
            }
            return null;
        }
        if(gwNode.getBusinessType() == null){
            return null;
        }
        return NodeBusinessType.valueOf(gwNode.getBusinessType());
    }

    public GwNode getGwNodeById(String id){
        return nodeFromChild.get(id);
    }

    public String getNodeNameByGwId(String id){
        if(organizationNode == null || organizationNode.getId().equals(id) || organizationNode.getChild() == null || organizationNode.getChild().isEmpty()){
            return null;
        }
        return getNodeNameById(organizationNode.getChild(), id);

    }
    private String getNodeNameById(List<GwNode> child, String id){
        for(GwNode gwNode: child){
            if(gwNode.getId().equals(id) && gwNode.getIsVmNode() != null && gwNode.getIsVmNode()){
                return gwNode.getName();
            }
            if(gwNode.getChild() != null && !gwNode.getChild().isEmpty()){
                String name = getNodeNameById(gwNode.getChild(),id);
                if(name != null){
                    return name;
                }
            }
        }
        return null;
    }

    @Override
    public GwNode getChildNodeByOrgId(String orgId){
        if (orgId == null) {
            return null;
        }
        for (GwNode node : nodeFromChild.values()) {
            if (orgId.equals(node.getOrgId())) {
                return node;
            }
        }
        for (GwNode node : nodeFromChild.values()) {
            Boolean isVmNode = node.getIsVmNode();
            if (isVmNode != null && isVmNode) {
                GwNode childByOrgId = node.getChildByOrgId(orgId);
                if (childByOrgId != null) {
                    return childByOrgId;
                }
            }
        }
        log.info("not fond");
        return null;
    }

    @Override
    public GwNode getByNodeId(String nodeId) {
        if (organizationNode == null) {
            return null;
        }
        return organizationNode.getByNodeIdFromTree(nodeId);
    }

    @Override
    public GwNode getChildByNodeId(String nodeId){
        if (nodeId == null) {
            return null;
        }
        for (GwNode node : nodeFromChild.values()) {
            if (node.getId().equals(nodeId)) {
                return node;
            }
            GwNode childByNodeId = node.getChildByNodeId(nodeId);
            if (childByNodeId != null) {
                return childByNodeId;
            }
        }
        log.info("not fond by node id");
        return null;
    }

    @Override
    public GwNode getNextUpperNode(String nodeId,boolean checkPermission) {
        if (organizationNode == null) {
            return null;
        }
        return organizationNode.getNextUpperNodeByNodeId(nodeId, checkPermission);
    }

    /**
     * 找到两个节点的最近公共父节点
     * @param targetId 目标节点node id
     * @param sourceId 当前节点node id
     */
    @Override
    public GwNode getCommonParentNode(String targetId, String sourceId) {
        String currentId = targetId;
        GwNode nextUpperNode;
        GwNode currentNode = getByNodeId(targetId);
        if (currentNode == null) {
            return null;
        }
        if (currentNode.getChildByNodeId(sourceId) != null) {
            return currentNode;
        }
        while ((nextUpperNode = getNextUpperNode(currentId,false)) != null) {
            if (nextUpperNode.getChildByNodeId(sourceId) != null) {
                return nextUpperNode;
            }else{
                currentNode = nextUpperNode;
                currentId = currentNode.getId();
            }
        }
        return null;
    }
    @Override
    public GwId findLocal(GwNode gwNode,String vmNodeId) {
        GwNode currentNode = gwNode.getByNodeIdFromTree(vmNodeId);
        if (currentNode != null && currentNode.isVm()) {
            GwNode parent = gwNode.getNextUpperNodeByNodeId(vmNodeId, false);
            while (parent != null && parent.isVm()) {
                parent = gwNode.getNextUpperNodeByNodeId(parent.getId(), false);
            }
            return parent == null ? null : parent.toGwId();
        }
        return null;
    }
}

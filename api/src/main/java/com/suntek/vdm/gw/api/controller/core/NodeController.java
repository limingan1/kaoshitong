package com.suntek.vdm.gw.api.controller.core;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.api.service.SecureService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.enums.NodeStatusType;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.common.pojo.node.NodeStatus;
import com.suntek.vdm.gw.common.util.ReflectUtil;
import com.suntek.vdm.gw.common.util.ResultHelper;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.conf.service.MeetingService;
import com.suntek.vdm.gw.core.annotation.PassLicense;
import com.suntek.vdm.gw.core.annotation.PassToken;
import com.suntek.vdm.gw.core.annotation.SystemConfig;
import com.suntek.vdm.gw.core.annotation.WriteLog;
import com.suntek.vdm.gw.core.api.request.node.*;
import com.suntek.vdm.gw.core.api.response.BasePageResponse;
import com.suntek.vdm.gw.core.api.response.node.DetailsResponse;
import com.suntek.vdm.gw.core.api.response.node.GetNodeTokenResponse;
import com.suntek.vdm.gw.core.api.response.node.GetRemoteInfoResponse;
import com.suntek.vdm.gw.core.api.response.node.PageResponse;
import com.suntek.vdm.gw.core.customexception.BaseStateException;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.enumeration.NodeType;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.pojo.TableDate;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.smc.response.GetTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

@RestController
@RequestMapping("/node")
public class NodeController {

    @Autowired
    private NodeConfigService nodeConfigService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private HttpServletRequest httpServletRequest;
    @Autowired
    private MeetingService meetingService;
    @Autowired
    private UserService userService;
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private HttpServletResponse httpServletResponse;
    @Autowired
    private SecureService secureService;

    @PassToken
    @PostMapping("/tokens")
    public ResponseEntity<String> getNodeTokens(@RequestBody GetNodeTokenRequest request, @RequestHeader("Authorization") String authorization) {
        try {
            String realIP = httpServletRequest.getHeader("X-Real-IP");
            if (realIP == null) {
                realIP = httpServletRequest.getRemoteAddr();
            }
            GetNodeTokenResponse response = nodeConfigService.getNodeTokens(request, realIP, authorization);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (BaseStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }


    @SystemConfig
    @PassLicense
    @GetMapping("/list")
    public ResponseEntity<String> list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "limit", required = false) Integer limit) {
        secureService.setResponseHeader(httpServletResponse);
        Map<String, Object> query = new HashMap<>();
        TableDate tableDate = nodeDataService.getPage(query, page, limit, null, null);
        BasePageResponse<PageResponse> basePageResponse = new BasePageResponse();
        basePageResponse.setCount(tableDate.getCount());
        List<PageResponse> pageResponses = new ArrayList<>();
        for (Object item : tableDate.getData()) {
            PageResponse add = new PageResponse();
            ReflectUtil.copyPropertiesSimple(item, add);
            if (add.getType().equals(NodeType.THIS.value())) {
                add.setNodeStatus(new NodeStatus(NodeStatusType.ONLINE, NodeStatusType.ONLINE));
            } else {
                add.setNodeStatus(nodeManageService.getNodeStatus(add.getId()));
            }
            pageResponses.add(add);
        }
        basePageResponse.setData(pageResponses);
        return new ResponseEntity<>(JSON.toJSONString(basePageResponse), HttpStatus.OK);
    }


    @SystemConfig
    @PassLicense
    @GetMapping("/local/ip")
    public ResponseEntity<String> getLocalIp() {
        secureService.setResponseHeader(httpServletResponse);
        return new ResponseEntity<>(JSON.toJSONString(SystemConfiguration.getSmcAddress()), HttpStatus.OK);
    }

    @SystemConfig
    @PassLicense
    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable String id) {
        secureService.setResponseHeader(httpServletResponse);
        NodeData nodeData = nodeDataService.getOneById(id);
        DetailsResponse response = new DetailsResponse();
        ReflectUtil.copyPropertiesSimpleAndSuper(nodeData, response);
        return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
    }

    @WriteLog(operation = {"添加","add"}, logType = "operation",source={"级联节点","cascade node"})
    @SystemConfig
    @PassLicense
    @PostMapping("")
    public ResponseEntity<String> add(@RequestBody AddNodeRequest request) {
        try {
            nodeConfigService.add(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @WriteLog(operation = {"修改","update"}, logType = "operation",source={"级联节点","cascade node"})
    @SystemConfig
    @PassLicense
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@RequestBody UpdateNodeRequest request, @PathVariable String id) {
        try {
            request.setId(id);
            nodeConfigService.update(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @WriteLog(operation = {"删除","delete"}, logType = "operation",source={"级联节点","cascade node"})
    @SystemConfig
    @PassLicense
    @DeleteMapping("/{id}")
    public ResponseEntity<String> del(@PathVariable("id") String id) {
        try {
            nodeConfigService.del(id);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @SystemConfig
    @PassLicense
    @GetMapping("/check/local")
    public ResponseEntity<Boolean> checkLocal() {
        NodeData nodeData = nodeDataService.getLocal();
        return new ResponseEntity<Boolean>(nodeData != null, HttpStatus.OK);
    }

    @PassToken
    @PassLicense
    @PostMapping("/check/add/remote")
    public ResponseEntity<String> checkRemoteNodeAdd(@RequestBody RemoteAddNodeVerifyRequest request,
                                                     @RequestHeader("Authorization") String authorization) {
        try {
            nodeConfigService.remoteAddNodeVerify(request, authorization);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PassToken
    @PassLicense
    @PostMapping("/check/add/local")
    public ResponseEntity<String> checkLocalNodeAdd(@RequestBody AddNodeVerifyRequest request) {

        try {
            nodeConfigService.addNodeVerify(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }

    }

    @PassToken
    @PassLicense
    @PostMapping("/loginSmc")
    public ResponseEntity<String> loginSmc(@RequestBody LoginSmcRequest request) {
        try {
            secureService.setResponseHeader(httpServletResponse);
            GetTokenResponse response = nodeConfigService.loginSmc(request);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PassLicense
    @PostMapping("/check/update/local")
    public ResponseEntity<String> checkLocalNodeUpdate(@RequestBody UpdateNodeVerifyRequest request) {
        try {
            nodeConfigService.updateNodeVerify(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PassToken
    @PassLicense
    @PostMapping("/check/update/remote")
    public ResponseEntity<String> checkRemoteNodeUpdate(@RequestHeader("Authorization") String authorization) {
        try {
            nodeConfigService.remoteUpdateNodeVerify(authorization);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PassToken
    @PassLicense
    @GetMapping("/remote/info")
    public ResponseEntity<String> getRemoteInfo(@RequestHeader("Authorization") String authorization) {
        try {
            GetRemoteInfoResponse response = nodeConfigService.getRemoteInfoHandler(authorization);
            return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }

    }


    @PostMapping("/sendTopTree")
    public ResponseEntity<String> sendTopTree(@RequestBody GwNode gwNode) {
        nodeManageService.sendTopTreeHandler(gwNode);
        meetingService.initCasConf();
        return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
    }

    @PostMapping("/sendLowTree")
    public ResponseEntity<String> sendLowTree(@RequestBody GwNode gwNode) {
        nodeManageService.sendLowTreeHandler(gwNode);
        meetingService.initCasConf();
        return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);

    }

    @PostMapping("/remoteNodeUpdate")
    public ResponseEntity<String> remoteNodeUpdateHandler(@RequestBody RemoteNodeUpdateRequest request) {
        nodeConfigService.remoteNodeUpdateHandler(request);
        return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
    }

    @PassLicense
    @GetMapping("/time")
    public ResponseEntity<String> time(@RequestHeader("token") String token) {
        LocalToken localToken = localTokenManageService.get(token);
        if (localToken == null) {
            return new ResponseEntity<>("token not fond", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
    }
    @PassLicense
    @GetMapping("/keepAlive")
    public ResponseEntity<String> keepAlive(@RequestHeader("token") String token) {
        try {
            secureService.setResponseHeader(httpServletResponse);
            userService.keepAlive(token);
            LocalToken localToken = localTokenManageService.get(token);
            localToken.setExpire(localToken.getExpire()+3*60*1000);
        } catch (BaseStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
    }
    @PassLicense
    @GetMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("token") String token) {
        try {
            userService.delTokens(token);
        } catch (BaseStateException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
    }
}

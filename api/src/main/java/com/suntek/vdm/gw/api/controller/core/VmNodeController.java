package com.suntek.vdm.gw.api.controller.core;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.api.service.SecureService;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.request.meeting.Organization;
import com.suntek.vdm.gw.common.util.ResultHelper;
import com.suntek.vdm.gw.conf.service.OtherService;
import com.suntek.vdm.gw.core.annotation.PassLicense;
import com.suntek.vdm.gw.core.annotation.PassToken;
import com.suntek.vdm.gw.core.annotation.SystemConfig;
import com.suntek.vdm.gw.core.annotation.WriteLog;
import com.suntek.vdm.gw.core.api.request.vm.AddVmNodeRequest;
import com.suntek.vdm.gw.core.api.request.vm.UpdateVmNodeRequest;
import com.suntek.vdm.gw.core.api.response.vm.VmDetailsResponse;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.core.service.VmNodeConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/node/vm")
public class VmNodeController {
    @Autowired
    private VmNodeConfigService vmNodeConfigService;
    @Autowired
    private NodeDataService nodeConfigService;
    @Autowired
    private OtherService otherService;
    @Autowired
    private HttpServletResponse httpServletResponse;
    @Autowired
    private SecureService secureService;
    @Value("${localOrgNodeDisplay}")
    private boolean localOrgNodeDisplay;

    @WriteLog(operation = {"添加","add"}, logType = "operation",source={"虚拟节点","vm node"})
    @SystemConfig
    @PassLicense
    @PostMapping("")
    public ResponseEntity<String> add(@RequestBody AddVmNodeRequest request) {
        try {
            vmNodeConfigService.add(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @WriteLog(operation = {"修改","update"}, logType = "operation",source={"虚拟节点","vm node"})
    @SystemConfig
    @PassLicense
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@RequestBody UpdateVmNodeRequest request, @PathVariable String id) {
        try {
            request.setId(id);
            vmNodeConfigService.update(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @WriteLog(operation = {"删除","delete"}, logType = "operation",source={"虚拟节点","vm node"})
    @SystemConfig
    @PassLicense
    @DeleteMapping("/{id}")
    public ResponseEntity<String> del(@PathVariable("id") String id) {
        try {
            vmNodeConfigService.del(id);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @SystemConfig
    @PassLicense
    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable String id) {
        VmNodeData vmNodeData = vmNodeConfigService.getOneById(id);
        VmDetailsResponse response = new VmDetailsResponse();
        BeanUtils.copyProperties(vmNodeData, response);
        return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
    }

    @SystemConfig
    @PassLicense
    @GetMapping("/list")
    public ResponseEntity<String> list() {
        secureService.setResponseHeader(httpServletResponse);
        VmDetailsResponse vmDetailsResponse = vmNodeConfigService.list();
        return new ResponseEntity<>(JSON.toJSONString(vmDetailsResponse), HttpStatus.OK);
    }

    @SystemConfig
    @PassLicense
    @PostMapping("/check/update")
    public ResponseEntity<String> checkLocalNodeUpdate(@RequestBody UpdateVmNodeRequest request) {
        try {
            vmNodeConfigService.updateNodeVerify(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @SystemConfig
    @PassLicense
    @PostMapping("/check/add")
    public ResponseEntity<String> checkLocalNodeAdd(@RequestBody AddVmNodeRequest request) {
        try {
            vmNodeConfigService.addNodeVerify(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }


    @SystemConfig
    @PassLicense
    @GetMapping("/organizations")
    public ResponseEntity<String> getOrganizations() {
        try {
            if (nodeConfigService.getLocal() == null) {
                return new ResponseEntity<String>("local node not config", HttpStatus.INTERNAL_SERVER_ERROR);
            }
            List<Organization> list = otherService.getOrganizations(CoreConfig.INTERNAL_USER_TOKEN);
            return new ResponseEntity<>(JSON.toJSONString(list), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>("[]", HttpStatus.OK);
        }
    }
    @SystemConfig
    @PassLicense
    @PassToken
    @GetMapping("/hidePermissionSwitch")
    public ResponseEntity<String> hidePermissionSwitch() {
        return new ResponseEntity<>("{\"hidePermissionSwitch\":" + !localOrgNodeDisplay + "}", HttpStatus.OK);
    }

}

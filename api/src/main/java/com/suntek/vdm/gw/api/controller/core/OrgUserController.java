package com.suntek.vdm.gw.api.controller.core;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.request.meeting.Organization;
import com.suntek.vdm.gw.common.util.ResultHelper;
import com.suntek.vdm.gw.conf.service.OtherService;
import com.suntek.vdm.gw.core.annotation.PassLicense;
import com.suntek.vdm.gw.core.annotation.SystemConfig;
import com.suntek.vdm.gw.core.api.request.orguser.AddOrgUserRequest;
import com.suntek.vdm.gw.core.api.request.orguser.OrgUserDetailsResponse;
import com.suntek.vdm.gw.core.api.request.orguser.UpdateOrgUserRequest;
import com.suntek.vdm.gw.core.api.request.vm.AddVmNodeRequest;
import com.suntek.vdm.gw.core.api.request.vm.UpdateVmNodeRequest;
import com.suntek.vdm.gw.core.api.response.vm.VmDetailsResponse;
import com.suntek.vdm.gw.core.entity.OrgUserData;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.service.orgUser.OrgUserConfigService;
import com.suntek.vdm.gw.core.service.VmNodeConfigService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/node/orguser")
public class OrgUserController {
    @Autowired
    private OrgUserConfigService orgUserConfigService;

    @PassLicense
    @SystemConfig
    @PostMapping("")
    public ResponseEntity<String> add(@RequestBody AddOrgUserRequest request) {
        try {
            orgUserConfigService.add(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PassLicense
    @SystemConfig
    @PutMapping("/{id}")
    public ResponseEntity<String> update(@RequestBody UpdateOrgUserRequest request, @PathVariable String id) {
        try {
            request.setId(id);
            orgUserConfigService.update(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PassLicense
    @SystemConfig
    @DeleteMapping("/{id}")
    public ResponseEntity<String> del(@PathVariable("id") String id) {
        try {
            orgUserConfigService.del(id);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PassLicense
    @SystemConfig
    @GetMapping("/{id}")
    public ResponseEntity<String> get(@PathVariable String id) {
        OrgUserDetailsResponse orgUserDetailsResponse = orgUserConfigService.getOneById(id);
        return new ResponseEntity<>(JSON.toJSONString(orgUserDetailsResponse), HttpStatus.OK);
    }

    @PassLicense
    @SystemConfig
    @GetMapping("/list/{id}")
    public ResponseEntity<String> list(@PathVariable("id") String id) {
        List<OrgUserDetailsResponse> list = orgUserConfigService.list(id);
        return new ResponseEntity<>(JSON.toJSONString(list), HttpStatus.OK);
    }

    @PassLicense
    @SystemConfig
    @PostMapping("/check/update")
    public ResponseEntity<String> checkLocalNodeUpdate(@RequestBody UpdateOrgUserRequest request) {
        try {
            orgUserConfigService.updateNodeVerify(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

    @PassLicense
    @SystemConfig
    @PostMapping("/check/add")
    public ResponseEntity<String> checkLocalNodeAdd(@RequestBody AddOrgUserRequest request) {
        try {
            orgUserConfigService.addNodeVerify(request);
            return new ResponseEntity<>(JSON.toJSONString(ResultHelper.success()), HttpStatus.OK);
        } catch (MyHttpException e) {
            return new ResponseEntity<>(e.getBody(), HttpStatus.valueOf(e.getCode()));
        }
    }

}

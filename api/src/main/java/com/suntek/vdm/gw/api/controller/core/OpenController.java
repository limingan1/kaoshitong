package com.suntek.vdm.gw.api.controller.core;

import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.pojo.OrganizationsResult;
import com.suntek.vdm.gw.conf.service.OtherService;
import com.suntek.vdm.gw.core.api.response.GetOrganizationsResponse;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.pojo.CascadeOrganization;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import com.suntek.vdm.gw.core.enumeration.NodeBusinessType;
import com.suntek.vdm.gw.core.service.NodeManageService;
import com.suntek.vdm.gw.core.service.NodeDataService;
import com.suntek.vdm.gw.common.util.CommonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/conf-portal")
@Slf4j
public class OpenController {

    @Autowired
    private NodeManageService nodeManageService;

    @Autowired
    private NodeDataService nodeDataService;

    @Autowired
    private OtherService otherService;

    @Value("${localOrgNodeDisplay}")
    private boolean localOrgNodeDisplay;

    @GetMapping("/cascade/organizations")
    public ResponseEntity<String> getOrganizations(@RequestHeader("Token") String token) {
        GetOrganizationsResponse response = new GetOrganizationsResponse();
        GwNode gwNode = nodeManageService.getOrganizationNode();
        GwNode moGwNode = JSON.parseObject(JSON.toJSONString(gwNode), GwNode.class);
        log.info("getOrganizations:{}", JSON.toJSONString(moGwNode));
        if (moGwNode == null) {
            response.setCode(1);
            NodeData localNodeData = nodeDataService.getLocal();
            CascadeOrganization cascadeOrganization = new CascadeOrganization();
            cascadeOrganization.setName(localNodeData.getName());
            cascadeOrganization.setCasOrgId(localNodeData.toGwId().toString());
            cascadeOrganization.setCasAreaCode(localNodeData.getAreaCode());
            cascadeOrganization.setLocal(true);
            cascadeOrganization.setBusinessType(localNodeData.getBusinessType());
            setChildBusinessType(cascadeOrganization.getChild());
            response.setData(cascadeOrganization);
        } else {
            response.setCode(1);
            NodeData localNodeData = nodeDataService.getLocal();
            //获取用户所在组织
            String orgId = otherService.getUserOrgId(token);
            OrganizationsResult organizationsResult = CommonHelper.organizationConversion(moGwNode, localNodeData.getId(), orgId, localOrgNodeDisplay);
            CascadeOrganization cascadeOrganization = organizationsResult.getCascadeOrganization();
            //默认全部不是空,
            if (cascadeOrganization.getName() == null) {
                cascadeOrganization.setName(localNodeData.getName());
                cascadeOrganization.setCasOrgId(localNodeData.toGwId().toString());
                cascadeOrganization.setCasAreaCode(localNodeData.getAreaCode());
                cascadeOrganization.setLocal(true);
                cascadeOrganization.setBusinessType(localNodeData.getBusinessType());
            }
            setChildBusinessType(cascadeOrganization.getChild());
            response.setData(cascadeOrganization);
        }
        return new ResponseEntity<>(JSON.toJSONString(response), HttpStatus.OK);
    }

    private int getBusinessType(Integer businessType) {
        if (NodeBusinessType.isWelinkOrCloudLink(businessType)) {
            return NodeBusinessType.WELINK.value();
        }
        return NodeBusinessType.SMC.value();
    }

    private void setChildBusinessType(List<CascadeOrganization> child) {
        if (child == null || child.isEmpty()) {
            return;
        }
        for (CascadeOrganization cascadeOrganization : child) {
            cascadeOrganization.setBusinessType(getBusinessType(cascadeOrganization.getBusinessType()));
            setChildBusinessType(cascadeOrganization.getChild());
        }
    }
}

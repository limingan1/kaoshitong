package com.suntek.vdm.gw.common.util;


import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.common.pojo.CascadeOrganization;
import com.suntek.vdm.gw.common.pojo.OrganizationsResult;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CommonHelper {
    // 相对路径
    private static String STATUS_CONFIG = "/ha/status";


    public static void sleep(long millisecond) {
        try {
            Thread.sleep(millisecond); //1000 毫秒，也就是1秒.
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }


    public static OrganizationsResult organizationConversion(GwNode gwNode, String localId, String orgId, boolean localOrgNodeDisplay) {
        OrganizationsResult organizationsResult = new OrganizationsResult();
        CascadeOrganization response = new CascadeOrganization();
        organizationsResult.setCascadeOrganization(response);
        response.setName(gwNode.getName());
        if (localId.equals(gwNode.getId())) {
            response.setLocal(true);
            if(StringUtils.isNotEmpty(gwNode.getMsg())){
                organizationsResult.setIsUseThis(true);
            }
        } else {
            response.setLocal(false);
        }
        response.setType(gwNode.getType());
        response.setCasOrgId(gwNode.toGwId().toString());
        response.setCasAreaCode(gwNode.getAreaCode());
        response.setBusinessType(gwNode.getBusinessType());
        response.setMsg(gwNode.getMsg());
        response.setNodestatus(gwNode.getNodestatus());
        //判断虚拟
//        if(response.isLocal()){
            response = dealVmOrganization(gwNode.getChild(), response, orgId, localOrgNodeDisplay, response.isLocal());
//        }
        response.setDisplayDirectlyUnder(gwNode.getDisplayDirectlyUnder());
        OrganizationsResult childOrganizationsResult = organizationConversionList(gwNode.getChild(), localId, orgId, localOrgNodeDisplay);
        if(childOrganizationsResult.getIsUseThis()){
            return childOrganizationsResult;
        }
        if(response.getChild() != null){
            response.getChild().addAll(childOrganizationsResult.getList());
        }else{
            response.setChild(childOrganizationsResult.getList());
        }
        return organizationsResult;
    }

    public static OrganizationsResult organizationConversionList(List<GwNode> gwNodes, String localId, String orgId, boolean localOrgNodeDisplay) {
        OrganizationsResult organizationsResult = new OrganizationsResult();
        List<CascadeOrganization> response = new ArrayList<>();
        organizationsResult.setList(response);
        if (gwNodes != null) {
            for (GwNode item : gwNodes) {
                OrganizationsResult childOrganizationsResult = organizationConversion(item, localId, orgId, localOrgNodeDisplay);
                if (childOrganizationsResult.getIsUseThis()) {
                    return childOrganizationsResult;
                }
                if(StringUtils.isNotEmpty(childOrganizationsResult.getCascadeOrganization().getMsg())){
                    continue;
                }
                response.add(childOrganizationsResult.getCascadeOrganization());
            }
        }
        return organizationsResult;
    }

    public static CascadeOrganization dealVmOrganization(List<GwNode> gwNodes, CascadeOrganization response, String orgId, boolean localOrgNodeDisplay, boolean isLocalNode) {
        for(int i=0; i< gwNodes.size(); i++){
            GwNode gwNode = gwNodes.get(i);
            if(gwNode.getIsVmNode() == null || !gwNode.getIsVmNode()){
                continue;
            }

            CascadeOrganization cascadeOrganization = response;
            //是否不隐藏
            if(localOrgNodeDisplay){
                cascadeOrganization = new CascadeOrganization();
            }
            cascadeOrganization.setName(gwNode.getName());
            cascadeOrganization.setCasOrgId(gwNode.toGwId().toString());
            cascadeOrganization.setCasAreaCode(gwNode.getAreaCode());
            cascadeOrganization.setType(gwNode.getType());
            cascadeOrganization.setBusinessType(1);
            cascadeOrganization.setMsg(gwNode.getMsg());
            cascadeOrganization.setNodestatus(gwNode.getNodestatus());
            cascadeOrganization.setDisplayDirectlyUnder(gwNode.getDisplayDirectlyUnder());

            if(gwNode.getOrgId() != null && gwNode.getOrgId().equals(orgId) && isLocalNode){
                cascadeOrganization.setLocal(true);
            }else{
                cascadeOrganization.setLocal(false);
            }
            List<CascadeOrganization> list = new ArrayList<>();
            Boolean hasLocal = dealVmChildOrganization(gwNode.getChild(), orgId, list, isLocalNode);

            cascadeOrganization.setChild(list);

            if(localOrgNodeDisplay){
                List<CascadeOrganization> childlist = new ArrayList<>();
                childlist.add(cascadeOrganization);
                response.setChild(childlist);
//            本级归属
                if(cascadeOrganization.isLocal() || hasLocal){
                    response.setLocal(false);
                }
            }else{
                if(!cascadeOrganization.isLocal() && !hasLocal && isLocalNode){
                    cascadeOrganization.setLocal(true);
                }
            }
            gwNodes.remove(i);
            break;
        }
        return response;
    }

    public static Boolean dealVmChildOrganization(List<GwNode> gwNodes,  String orgId, List<CascadeOrganization> list, boolean isLocalNode) {
        if(gwNodes == null || gwNodes.isEmpty()){
            return false;
        }
        Boolean hasLocal = false;
        for(int i=0; i< gwNodes.size(); i++){
            CascadeOrganization cascadeOrganization = new CascadeOrganization();
            GwNode gwNode = gwNodes.get(i);
            cascadeOrganization.setName(gwNode.getName());
            cascadeOrganization.setCasOrgId(gwNode.toGwId().toString());
            cascadeOrganization.setCasAreaCode(gwNode.getAreaCode());
            cascadeOrganization.setType(gwNode.getType());
            cascadeOrganization.setBusinessType(1);
            cascadeOrganization.setMsg(gwNode.getMsg());
            cascadeOrganization.setNodestatus(gwNode.getNodestatus());
            if(gwNode.getOrgId() != null && gwNode.getOrgId().equals(orgId) && isLocalNode){
                cascadeOrganization.setLocal(true);
            }else{
                cascadeOrganization.setLocal(false);
            }
            List<CascadeOrganization> childList = new ArrayList<>();
            Boolean hasLocalChild = dealVmChildOrganization(gwNode.getChild(), orgId, childList, isLocalNode);
            if(!hasLocal){
                hasLocal = hasLocalChild;
            }
            cascadeOrganization.setChild(childList);
            list.add(cascadeOrganization);
        }
        return hasLocal;
    }

    // 传入jar包对应目录
    public static boolean isStandbyCurrent(String dir) {
        boolean bStandbyCurrent = false;
        try {
            String[] cmd = new String[]{"cat", dir + "/../../base" + STATUS_CONFIG};
            ProcessBuilder builder = new ProcessBuilder(cmd);
            builder.redirectErrorStream(true);
            Process pro = builder.start();

            pro.waitFor();
            //读取命令执行的结果
            InputStreamReader input = new InputStreamReader(pro.getInputStream());
            BufferedReader reader = new BufferedReader(input);
            // 运行状态
            String msg;
            while ((msg = reader.readLine()) != null) {
                if (msg.contains("slave")) {
                    bStandbyCurrent = true;
                }
            }
        } catch (Exception e) {
            System.out.println("isStandbyCurrent error, {}" + e);
        }
        return bStandbyCurrent;
    }

    public static <T> T copyBean(Object source, Class<T> clazz) {
        return JSON.parseObject(JSON.toJSONString(source), clazz);
    }

}
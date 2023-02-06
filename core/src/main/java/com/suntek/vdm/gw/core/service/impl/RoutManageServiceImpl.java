package com.suntek.vdm.gw.core.service.impl;


import com.alibaba.fastjson.JSON;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.pojo.LocalToken;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.core.util.Trie;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.pojo.GwId;
import com.suntek.vdm.gw.common.pojo.node.GwNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RoutManageServiceImpl implements RoutManageService {

    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private VmNodeDataService vmNodeDataService;
    @Autowired
    private LocalTokenManageService localTokenManageService;
    @Autowired
    private NodeManageService nodeManageService;
    /**
     * 远端节点 最近节点
     */
    static Map<String, String> NODE_NODE_ROUT_MAP = new ConcurrentHashMap<>();

    /**
     * 地区编码  最近节点ID 映射
     */
    static Map<String, String> CODE_NODE_MAPPING_MAP = new ConcurrentHashMap<>();

    /**
     * 节点ID 地区编码  映射
     */
    static Map<String, String> NODE_CODE_MAPPING_MAP = new ConcurrentHashMap<>();


    /**
     * 路由树
     */
    static Trie NODE_TRIE = new Trie();


    public void generateTrie(@Nullable GwNode gwNode) {
        if (gwNode == null) {
            return;
        }
        Trie trie = new Trie();
        List<GwNode> all = gwNode.all();
        for (GwNode item : all) {
            trie.insert(item.getAreaCode(),item.toGwId());
        }
        NODE_TRIE = trie;
    }


    public boolean isLocal(GwId gwId) {
        NodeData local = nodeDataService.getLocal();
        if (local.getId().equals(gwId.getNodeId())) {
            return true;
        }
        if (local.getAreaCode().equals(gwId.getAreaCode())) {
            return true;
        }
        return false;
    }
    @Override
    public boolean isLocal(GwId gwId,String token) {
        LocalToken localToken = localTokenManageService.get(token);
        VmNodeData vmNode = vmNodeDataService.getOneByOrgId(localToken == null ? null : localToken.getOrgId());
        if (vmNode == null) {
            return isLocal(gwId);
        }
        if (gwId.getNodeId().equals(vmNode.getId())) {
            return true;
        }
        if (gwId.getAreaCode().equals(vmNode.getAreaCode())) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isLocalVm(String token){
        VmNodeData vmNode = vmNodeDataService.getOneByToken(token);
        return vmNode != null;
    }

    public GwId getCompleteGwIdBy(GwId gwId) {
        GwId completeGwId = NODE_TRIE.search(gwId.getAreaCode());
        return completeGwId;
    }

    public GwId getWayByGwId(GwId gwId) {
        String nodeId = NODE_NODE_ROUT_MAP.get(gwId.getNodeId());
        if (nodeId == null) {
            nodeId = CODE_NODE_MAPPING_MAP.get(gwId.getAreaCode());
        }
        if (nodeId == null) {
            return null;
        } else {
            String areaCode = NODE_CODE_MAPPING_MAP.get(nodeId);
            return new GwId(nodeId, areaCode);
        }
    }

    @Override
    public void generateRoute(GwNode gwNode) {
        String id = gwNode.getId();
        log.info("generate route id:{}", id);
        //先清除旧的
        cleanRoute(id);
        //添加地区编码
        NODE_CODE_MAPPING_MAP.put(id, gwNode.getAreaCode());
        List<GwNode> gwNodes = gwNode.all();
        log.info("generate route data:{}", JSON.toJSONString(gwNodes));
        for (GwNode item : gwNodes) {
            log.info("add rout by id:{},areaCode:{}", item.getAreaCode(), item.getId());
            if(item.isVm()){
                //本级虚拟节点，路由直接设置自己
                VmNodeData vmNodeData = vmNodeDataService.getOneById(item.getId());
                if(vmNodeData != null){
                    log.info("is local Vm node:{},areaCode:{}", item.getAreaCode(), item.getId());
                    NODE_NODE_ROUT_MAP.put(item.getId(), item.getId());
                    CODE_NODE_MAPPING_MAP.put(item.getAreaCode(), item.getId());
                    continue;
                }else{
                    GwId local = nodeManageService.findLocal(gwNode, item.getId());
                    if (local != null) {
                        log.info("add rout by local:{},areaCode:{}", local.getAreaCode(), local.getNodeId());
                        if (!NODE_NODE_ROUT_MAP.containsKey(item.getId())) {
                            NODE_NODE_ROUT_MAP.put(item.getId(), local.getNodeId());
                        }
                        if (!CODE_NODE_MAPPING_MAP.containsKey(item.getAreaCode())) {
                            CODE_NODE_MAPPING_MAP.put(item.getAreaCode(), local.getNodeId());
                        }
                    }
                }
            }
            //路由缓存
            NODE_NODE_ROUT_MAP.put(item.getId(), id);
            CODE_NODE_MAPPING_MAP.put(item.getAreaCode(), id);
        }
    }


    /**
     * 清除路由
     *
     * @param id
     */
    public void cleanRoute(String id) {
        log.info("del route id:{}", id);
        Iterator<Map.Entry<String, String>> it1 = NODE_NODE_ROUT_MAP.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry<String, String> entry = it1.next();
            String key = entry.getKey();
            String value = entry.getValue();
            if (value.equals(id)) {
                it1.remove();
            }
        }
        Iterator<Map.Entry<String, String>> it2 = CODE_NODE_MAPPING_MAP.entrySet().iterator();
        while (it2.hasNext()) {
            Map.Entry<String, String> entry = it2.next();
            String key = entry.getKey();
            String value = entry.getValue();
            if (value.equals(id)) {
                it2.remove();
            }
        }
    }
}

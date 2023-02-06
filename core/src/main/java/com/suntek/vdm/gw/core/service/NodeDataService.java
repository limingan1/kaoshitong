package com.suntek.vdm.gw.core.service;

import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.common.pojo.GwId;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author scorpios
 * @since 2021-06-10
 */
public interface NodeDataService extends ServiceFactory<NodeData> {
    NodeData getLocal();

    NodeData getTop();

    List<NodeData> getLow();

    List<NodeData> getNotLocal();

    NodeData getOneByAreaCode(String areaCode);

    NodeData getOneByGwId(GwId gwId);

    void updatePrimaryKey(String newId,String oldId);

    boolean isSuperTop();

}

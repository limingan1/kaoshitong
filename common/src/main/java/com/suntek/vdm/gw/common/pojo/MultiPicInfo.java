package com.suntek.vdm.gw.common.pojo;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
public class MultiPicInfo {
    /**
     * 画面数
     */
    private Integer picNum;

    /**
     * 模式
     */
    private Integer mode;

    /**
     * 子画面列表
     */
    private List<SubPic> subPicList;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiPicInfo that = (MultiPicInfo) o;
        return Objects.equals(picNum, that.picNum) &&
                Objects.equals(mode, that.mode) &&
                Objects.equals(subPicList, that.subPicList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(picNum, mode, subPicList);
    }

    public String getFirstParticipantId() {
        if (CollectionUtils.isEmpty(subPicList)) {
            return null;
        }
        return getSubPicList().get(0).getParticipantId();
    }

    public static MultiPicInfo valueOfDefault(SubPic subPic) {
        MultiPicInfo multiPicInfo = new MultiPicInfo();
        multiPicInfo.setPicNum(1);
        multiPicInfo.setMode(1);
        List<SubPic> list = new ArrayList<>();
        list.add(subPic);
        multiPicInfo.setSubPicList(list);
        return multiPicInfo;
    }

}
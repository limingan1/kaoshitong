package com.suntek.vdm.gw.smc.response.meeting.control;

import com.suntek.vdm.gw.smc.pojo.PresetMultiPicDto;

import lombok.Data;

import java.util.List;

@Data
public class GetPresetParamResponse  {
    /**
     * 预置多画面信息
     */
    private List<PresetMultiPicDto> presetMultiPics;
}

package com.suntek.vdm.gw.common.pojo;


public class GetParticipantsDetailInfoResponse  {
    /**
     * 会场名称
     */
    private String name;

    /**
     * 会场标识
     */
    private String uri;

    /**
     * MCU名称
     */
    private String mcuName;

    /**
     * 速率
     */
    private Integer rate;

    /**
     * 通信协议
     */
    private Integer ipProtocolType;

    /**
     * 音频协议
     */
    private Integer audioProtocol;

    /**
     * 视频协议
     */
    private Integer videoProtocol;

    /**
     * 视频能力
     */
    private Integer videoResolution;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMcuName() {
        return mcuName;
    }

    public void setMcuName(String mcuName) {
        this.mcuName = mcuName;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public Integer getIpProtocolType() {
        return ipProtocolType;
    }

    public void setIpProtocolType(Object obj) {
        if (obj instanceof Integer) {
            this.ipProtocolType = (Integer) obj;
        }
        if (obj instanceof String) {
            switch ((String) obj) {
                case "H323":
                    this.ipProtocolType = 0;
                    break;
                case "SIP":
                    this.ipProtocolType = 1;
                    break;
                case "DOUBLE_PROTOCOL":
                default:
                    this.ipProtocolType = 2;
                    break;
            }
        }
    }

    public Integer getAudioProtocol() {
        return audioProtocol;
    }

    public void setAudioProtocol(Integer audioProtocol) {
        this.audioProtocol = audioProtocol;
    }

    public Integer getVideoProtocol() {
        return videoProtocol;
    }

    public void setVideoProtocol(Integer videoProtocol) {
        this.videoProtocol = videoProtocol;
    }

    public Integer getVideoResolution() {
        return videoResolution;
    }

    public void setVideoResolution(Integer videoResolution) {
        this.videoResolution = videoResolution;
    }
}

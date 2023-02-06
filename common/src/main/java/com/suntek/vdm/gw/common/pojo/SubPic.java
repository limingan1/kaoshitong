package com.suntek.vdm.gw.common.pojo;

import lombok.Data;

import java.util.Objects;

@Data
public class SubPic {

    private String name;

    private String uri;

    /**
     * 会场Id(36字符)
     */
    private String participantId;

    /**
     * 视频流类型
     */
    private Integer streamNumber;

    public SubPic() {
    }

    public SubPic(String name, String uri, String participantId, Integer streamNumber) {
        this.name = name;
        this.uri = uri;
        this.participantId = participantId;
        this.streamNumber = streamNumber;
    }

    public void setStreamNumber(Object obj) {
        if (obj instanceof Integer) {
            this.streamNumber = (Integer) obj;
        }
        if (obj instanceof String) {
            switch ((String) obj) {
                case "RANGE_ONE":
                    this.streamNumber = 1;
                    break;
                case "RANGE_ZERO":
                default:
                    this.streamNumber = 0;
                    break;
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubPic subPic = (SubPic) o;
        return Objects.equals(name, subPic.name) &&
                Objects.equals(uri, subPic.uri) &&
                Objects.equals(participantId, subPic.participantId) &&
                Objects.equals(streamNumber, subPic.streamNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uri, participantId, streamNumber);
    }
}
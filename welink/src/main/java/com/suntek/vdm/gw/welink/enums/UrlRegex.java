package com.suntek.vdm.gw.welink.enums;

public enum UrlRegex {

    MEETING_DETAIL("/conf-portal/online/conferences/([^/ ]+)/detail"), //会议详情
    PARTICIPANTS_LIST("/conf-portal/online/conferences/([^/ ]+)/participants/conditions"), //会场列表
    PARTICIPANT_DETAIL("/conf-portal/online/conferences/([^/ ]+)/participants/([^/ ]+)/detailInfo"), //会场详情
    PARTICIPANTS_CONTROL("/conf-portal/online/conferences/([^/ ]+)/participants/status"), //会场管控
    ADD_DEL_PARTICIPANTS("/conf-portal/online/conferences/([^/ ]+)/participants"), //添加删除会场
    MEETING_CONTROL("/conf-portal/online/conferences/([^/ ]+)/status"),     //会议控制
    MEETING_DURATION("/conf-portal/online/conferences/([^/ ]+)/duration"),  //延长会议
    DELETE_MEETING("/conf-portal/online/conferences/([^/ ]+)"),  //结束会议
    SUBSCRIBE_PARTICIPANT_CONTROL("/topic/conferences/([^/ ]+)/participants/general"),
    PARTICIPANT_CONTROL("/conf-portal/online/conferences/([^/ ]+)/participants/([^/ ]+)/status"),  //会场控制
    QUERY_ADDRESS_BOOK("/conf-portal/addressbook/organizations/([^/ ]+)"),  //查询通讯录
    CAS_CONFERENCE_INFOS("/conf-portal/cascade/casConferenceInfos/([^/ ]+)"),
    GET_ONE_CONFERENCE("/conf-portal/conferences/([^/ ]+)")
    ;

    private String regex;

    UrlRegex(String value) {
        this.regex = value;
    }
    public String getRegex() {
        return regex;
    }
}

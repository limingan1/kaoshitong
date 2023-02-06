package com.suntek.vdm.gw.common.enums;

public enum ConfApiUrl {
    CASCADE_NOTIFY_SOURCE("/conf-portal/cascade/channel/notify"),
    CASCADE_SUBSCRIBE("/conf-portal/cascade/subscribe"),
    CASCADE_FREE_CHANNEL("/conf-portal/cascade/channel/free"),
    CASCADE_CHANNEL_STATUS("/conf-portal/cascade/channel/status"),
    CONFERENCES("/conf-portal/conferences"),
    CONFERENCES_CONDITIONS("/conf-portal/conferences/conditions?page=%s&size=%s"),
    SEND_MAIL("/conf-portal/mail/conferences/%s"),
    CONFERENCES_PERIOD("/conf-portal/conferences/period/%s"),
    DEL_MEETING("/conf-portal/online/conferences/%s?confCasId=%s&keepByCasState=%s"),
    GET_ADDRESS_BOOK_ROOMS("/conf-portal/addressbook/rooms?id=%s&searchType=%s&keyWord=%s&casOrgId=%s"),
    CONTROLLER_PARTICIPANT_STATUS("/conf-portal/online/conferences/%s/participants/%s/status"),
    CONTROLLER_PARTICIPANTS_STATUS("/conf-portal/online/conferences/%s/participants/status"),
    SET_TEXT_TIPS("/conf-portal/online/conferences/%s/textTips"),
    SET_PARTICIPANT_TEXT_TIP("/conf-portal/online/conferences/%s/participants/$s/textTips"),
    DURATION("/conf-portal/online/conferences/%s/duration"),
    DELETE_PANTICIPANTS("/conf-portal/online/conferences/%s/participants"),
    GET_DETAIL("/conf-portal/online/conferences/%s/detail?confCasId=%s"),
    GET_CONDITIONS("/conf-portal/online/conferences/%s/participants/conditions?confCasId=%s&page=%s&size=%s"),
    ADD_PARTICAPANTS("/conf-portal/online/conferences/%s/participants?confCasId=%s"),
    ADD_ATTENDEES("/conf-portal/online/conferences/%s/attendees"),
    CAMERA_CONTROL("/conf-portal/online/conferences/%s/participants/%s/camera"),
    CHAT_MIC("/conf-portal/online/conferences/%s/chat/mic"),
    CHAT_SPEAKER("/conf-portal/online/conferences/%s/chat/speaker"),
    GET_PARTICIPANT_DETAILINFO("/conf-portal/online/conferences/%s/participants/%s/detailInfo"),
    GET_PARTICIPANT_CAPABILITY("/conf-portal/online/conferences/%s/participants/%s/capability"),
    SET_COMMONLY_USED_PARTICIPANTS("/conf-portal/online/conferences/%s/participants/order"),
    SET_REMIND("/conf-portal/online/conferences/%s/participants/%s/remind"),
    SET_PARTICIPANTS_PARAMETER("/conf-portal/online/conferences/%s/participants/%s/parameter"),
    GET_VIDEO_SOURCE("/conf-portal/online/conferences/%s/participants/videoSource"),
    GET_BRIEFS("/conf-portal/online/conferences/%s/participants/briefs"),
    MIGRATE("/conf-portal/online/conferences/%s/migrate"),
    BATCH_TEXT_TIIPS("/conf-portal/online/conferences/%s/participants/batchTextTips"),
    PUSH_AI_CAPTION("/conf-portal/online/conferences/\"+conferenceId+\"/participants/ai/caption"),
    CONTROLLER_CONFERENCE_STATUS("/conf-portal/online/conferences/%s/status"),
    CONTROLLER_CONFERENCE_STATUS_TOP("/conf-portal/online/conferences/%s/status/top/%s"),
    CONTROLLER_CONFERENCE_STATUS_DIRECT("/conf-portal/online/conferences/%s/status/direct"),
    PARTICIPANT_TO_PULL_SOURCE("/conf-portal/online/conferences/%s/participants/%s/status/pull"),
    QUICKHANGUP("/conf-portal/online/conferences/quickHangup"),
    CALLINFO("/conf-portal/online/onferences/callInfo"),
    CASCADE_ADD_CHANNEL("/conf-portal/cascade/addCasChannel"),
    CASCADE_CONFERENCE_INFOS("/conf-portal/cascade/casConferenceInfos/%s"),
    GET_ONE_CONFERENCE("/conf-portal/conferences/%s?confCasId=%s"),
    MERGE_CONFERENCE("/conf-portal/online/conferences/%s/merge"),
    CHANGESITENAME("/conf-portal/online/conferences/%s/participants/param"),
    ;




    private String value;

    private ConfApiUrl(String value) {     //必须是private的，否则编译错误
        this.value = value;
    }

    public String value() {
        return this.value;
    }

}

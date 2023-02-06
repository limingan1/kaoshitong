package com.suntek.vdm.gw.smc.adaptService.util;

public enum SubscribeUrlRegex {
    CONFERENCES_STATUS_REGEX("/topic/conferences/status"),
    CONFERENCES_CONTROL_STATUS_REGEX("/topic/conferences/([^/ ]+)"),
    CONFERENCES_PARTICIPANT_STATUS_REGEX("/topic/conferences/([^/ ]+)/participants/general");

    private String regex;

    SubscribeUrlRegex(String value) {
        this.regex = value;
    }
    public String getRegex() {
        return regex;
    }
}

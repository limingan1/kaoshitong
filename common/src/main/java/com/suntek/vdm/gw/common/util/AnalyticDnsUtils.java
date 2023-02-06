package com.suntek.vdm.gw.common.util;


import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class AnalyticDnsUtils {
    static InetAddress[] myServer = null;
    static InetAddress myIPaddress = null;

    public static InetAddress[] getServerIP(String domainName) throws Exception {
        try {
            myServer = InetAddress.getAllByName(domainName);
        } catch (UnknownHostException e) {
            log.error("Analytic failed");
            log.error(e.getMessage());
            throw new Exception(e);
        }
        return (myServer);
    }

    public static InetAddress getMyIP() {
        try {
            myIPaddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            log.error("Analytic failed");
        }
        return (myIPaddress);
    }
}

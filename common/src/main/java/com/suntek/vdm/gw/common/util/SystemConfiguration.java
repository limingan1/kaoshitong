package com.suntek.vdm.gw.common.util;

import com.suntek.vdm.gw.common.enums.SmcVersionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
public class SystemConfiguration {
    private static Pattern SMC_TYPE = Pattern.compile("smc_type=(.*)");
    private static Pattern SMC_SMCADDRESS = Pattern.compile("smc_smcaddress=(.*)");
    private static Pattern SMC_SMCDOMAIN = Pattern.compile("domain=(.*)");

    private static String smcVersion;
    private static String smcAddress;
    private static Boolean isDomain = false;

    public static SmcVersionType getSmcVersion() {
        if (smcVersion == null) {
            return SmcVersionType.V3;
        }
        return SmcVersionType.valueOfString(smcVersion);
    }


    public static boolean smcVersionIsV2() {
        if (getSmcVersion().equals(SmcVersionType.V3)) {
            return false;
        }
        return true;
    }

    public static boolean smcVersionIsV3() {
        return !smcVersionIsV2();
    }


    public static String getSmcAddress() {
        return smcAddress;
    }

    public static Boolean isDomain() {
        return isDomain;
    }

    public static void setSmcAddress(String smcAddress) {
        SystemConfiguration.smcAddress = smcAddress;
    }

    public static void init(String path, String defaultSmcVersion, String defaultSmcAddress) {
        if (path.contains("classpath:")) {
            String relativePath = path.replace("classpath:", "");
            String classPath = System.getProperties().getProperty("user.dir");
            path = classPath + relativePath;
        }
        path = path + "/user.ini";
        log.info(path);
        FileInputStream fs = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        try {
            fs = new FileInputStream(path);
            ir = new InputStreamReader(fs);
            br = new BufferedReader(ir);
            String line;
            String domian = null;
            String smc_smcaddress = null;
            while ((line = br.readLine()) != null) {
                Matcher matcher1 = SMC_TYPE.matcher(line);
                if (matcher1.find()) {
                    log.info("find:{}", line);
                    smcVersion = matcher1.group(1);
                }
                Matcher matcher2 = SMC_SMCADDRESS.matcher(line);
                if (matcher2.find()) {
                    log.info("find:{}", line);
                    smc_smcaddress = matcher2.group(1);
                    if (smc_smcaddress.contains(":")) {
                        smc_smcaddress = smc_smcaddress.split(":")[0];
                    }
                }

                Matcher matcher3 = SMC_SMCDOMAIN.matcher(line);
                if (matcher3.find()) {
                    log.info("find:{}", line);
                    domian = matcher3.group(1);
                    if (domian.contains(":")) {
                        domian = domian.split(":")[0];
                    }
                }
            }
            if(StringUtils.isNotEmpty(domian)){
                smcAddress = domian;
                isDomain = true;
            }else {
                smcAddress = smc_smcaddress;
            }
        } catch (Exception e) {
            smcVersion = defaultSmcVersion;
            smcAddress = defaultSmcAddress;
            log.warn("failed to read the file use the default value");
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (ir != null) {
                    ir.close();
                }
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException e) {
            }
        }
    }
}

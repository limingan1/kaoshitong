package com.suntek.vdm.gw.conf.dual;

import com.alibaba.fastjson.JSONObject;
import com.suntek.vdm.gw.common.customexception.MyHttpException;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import com.suntek.vdm.gw.common.pojo.request.DualHostDto;
import com.suntek.vdm.gw.common.service.HttpService;
import com.suntek.vdm.gw.common.util.dual.ExecUtil;
import com.suntek.vdm.gw.common.util.dual.Host;
import com.suntek.vdm.gw.common.util.dual.IpAddressUtils;
import com.suntek.vdm.gw.common.util.security.EncryptionMachine;
import com.suntek.vdm.gw.conf.service.internal.InternalLinkService;
import com.suntek.vdm.gw.core.cache.CommonCache;
import com.suntek.vdm.gw.core.service.StartService;
import com.suntek.vdm.gw.core.task.Scheduler;
import com.suntek.vdm.gw.license.service.LicenseManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.*;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DualServiceImpl implements DualService {
    @Value("${cas.floating_ip}")
    private String floatingIp;
    @Value("${cas.security.file_path}")
    private String securityFilePath;
    @Value("${cas.user.config.file_path}")
    private String userConfigPath;
    @Autowired
    private Scheduler scheduler;
    @Qualifier("httpServiceImpl")
    @Autowired
    private HttpService httpService;
    @Autowired
    @Lazy
    private TimerJobService timerJobService;
    @Autowired
    private StartService startService;
    @Autowired
    private LicenseManagerService licenseManagerService;
    @Autowired
    private InternalLinkService internalLinkService;
    @Autowired
    private ExecUtil execUtil;


    private static boolean IsReady = false;
    private static Boolean IsMasterReady = false;

    @Override
    public DualHostDto dualHostReady(DualHostDto dualHostDto) {
        String hostName = dualHostDto.getHost();
        boolean result = false;
        if(!dualHostDto.getIsReady()){
            String CLASS_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            String url = "jdbc:sqlserver://localhost:1433;integratedSecurity=true;DatabaseName=SMCDistribution;";

            Connection conn = null;
            Statement stat = null;
            try {
                Class.forName(CLASS_NAME);
                conn = DriverManager.getConnection(url);
                stat = conn.createStatement();
                result = checkIsMasterReady(stat);
                log.info("result: {}",result);
                if(result){
                    IsReady = true;
                }
                if (null != stat) {
                    stat.close();
                }
                if (null != conn) {
                    conn.close();
                }
            } catch (SQLException e) {
                log.error("stackTrace {}",e.getStackTrace());
                log.error(e.getMessage());
            } catch (ClassNotFoundException e) {
                log.error("stackTrace {}",e.getStackTrace());
                log.error(e.getMessage());
            }
        }
        Host host = Host.getInstance();
        if(!host.getMasterHostName().equals(hostName)){
            host.setSlaveHostName(hostName);
        }

        DualHostDto dualHostResp = new DualHostDto();
        dualHostResp.setHost(host.getMasterHostName());
        if(result || IsReady){
            dualHostResp.setIsReady(true);
        }

        if(dualHostDto.getIsGetKey() != null && dualHostDto.getIsGetKey()){
            String workKey = EncryptionMachine.bytesToHex(EncryptionMachine.workFileNameKey) + EncryptionMachine.bytesToHex(EncryptionMachine.workFileNameIv);
            dualHostResp.setIsGetKey(true);
            dualHostResp.setKey(workKey);
        }

        return dualHostResp;
    }

    private boolean checkIsMasterReady(Statement stat) throws SQLException {
        ResultSet resultSet = stat.executeQuery("SELECT * FROM msdb.dbo.sysjobs where Name LIKE '%-vdmcas3-cascadegw_Publication-%' and category_id = 15");
//      select * from MSreplication_monitordata where agent_name like '%-vdmcas-cascadegw_Publication-%'
        if (resultSet.next()) {
            return true;
        }
        return false;
    }

    @Override
    @Async("taskExecutor")
    public void start() {

        List<String> localIPList = IpAddressUtils.getLocalIPList();
        log.info("ip:"+ localIPList.toString());
        boolean isHost = false;
        for(String localIp : localIPList) {
            if(localIp.equals(floatingIp)) {
                isHost = true;
                log.info("ip:"+ localIp);
                break;
            }
        }
        if(isHost){
            log.info("this is master host.");
        }else{
            log.info("This is a standby machine");
        }
        boolean finalIsHost = isHost;
        scheduler.scheduledThreadPoolLaterDo(0, TimeUnit.SECONDS, new Runnable() {
            @Override
            public void run() {
                checkMasterAndSlaveHostName(finalIsHost,false,0);
            }
        });

    }

    public void checkMasterAndSlaveHostName(boolean isMaster,boolean isRunCmd, int tryToCheckMasterJob){
        List<String> localIPList = IpAddressUtils.getLocalIPList();
        boolean isHost = false;
        for(String localIp : localIPList) {
            if(localIp.equals(floatingIp)) {
                isHost = true;
                log.info("ip:"+ localIp);
                break;
            }
        }
        isMaster = isHost;
        Host host = Host.getInstance();
        host.setMaster(isMaster);
        host.setDual(true);
        log.info("SlaveHostName is: " + host.getSlaveHostName()+"  isMaster: " + isMaster + ", isRunCmd: "+isRunCmd);

        if(!isMaster){
            String url = "https://"+floatingIp+":5443/conf-portal/dual/hostname";
            DualHostDto dualHostDto = new DualHostDto();
            dualHostDto.setHost(host.getMasterHostName());
            if(tryToCheckMasterJob>10){
                tryToCheckMasterJob = 0;

            }else{
                dualHostDto.setIsReady(IsMasterReady);
            }

            String responseBody = null;
            try {
                responseBody = httpService.post(url, dualHostDto, null).getBody();
            } catch (MyHttpException e) {
                log.error("stackTrace {}",e.getStackTrace());
                log.error(e.getMessage());
            }

//            HttpResponse response = null;
//            try{
//                response = post(url, null, "");
//            }catch (Exception e){
//                LOG.info(e);
//                cancelSlave(host);
//            }
            if(responseBody!=null){
                dualHostDto =  JSONObject.parseObject(responseBody, DualHostDto.class);

                String slaveHostName = dualHostDto.getHost();
                boolean isMasterReady = dualHostDto.getIsReady();

                if(slaveHostName != null && !slaveHostName.isEmpty() && !slaveHostName.equals(host.getMasterHostName()) && isMasterReady){
                    IsMasterReady = true;
                    host.setSlaveHostName(slaveHostName);
                    if(!isRunCmd){
                        log.info("set slavename " + slaveHostName+" master is ready");
                        //获取wordKey，并加密保存
                        getAndSaveWordKey(host);
                        final boolean nextIsHost = isMaster;
                        scheduler.scheduledThreadPoolLaterDo(10, TimeUnit.SECONDS, new Runnable() {
                            @Override
                            public void run() {
                                timerJobService.restart(5L,nextIsHost);
                            }
                        });
                        excureCmd(isMaster,host);
                        isRunCmd = true;
                    }
                }else{
                    cancelSlave(host);
                }

            }
        }
        if(isMaster && !host.getSlaveHostName().isEmpty()){
            final boolean nextIsHost = isMaster;
            scheduler.scheduledThreadPoolLaterDo(10, TimeUnit.SECONDS, new Runnable() {
                @Override
                public void run() {
                    timerJobService.restart(5L,nextIsHost);
                }
            });
            excureCmd(isMaster,host);
            return;
        }

        final boolean bNextMaster = isMaster;
        final boolean bNextIsRunCmd = isRunCmd;
        final int intTryToCheckMasterJob = ++tryToCheckMasterJob;
        scheduler.scheduledThreadPoolLaterDo(10, TimeUnit.SECONDS, new Runnable() {
            @Override
            public void run() {
                checkMasterAndSlaveHostName(bNextMaster,bNextIsRunCmd,intTryToCheckMasterJob);
            }
        });
    }

    private Boolean getAndSaveWordKey(Host host){
        String url = "https://"+floatingIp+":5443//conf-portal/dual/hostname";
        DualHostDto dualHostDto = new DualHostDto();
        dualHostDto.setHost(host.getMasterHostName());
        dualHostDto.setIsGetKey(true);
        dualHostDto.setIsReady(IsMasterReady);
        HttpHeaders httpHeaders = null;
        try {
            httpHeaders = httpService.post(url, dualHostDto, null).getHeaders();

        } catch (MyHttpException exception) {
            exception.printStackTrace();
        }
        List<String> keys = httpHeaders.get("casuuid");
        if(keys != null && keys.size()>0) {
            String key = keys.get(0);
            try {
                String enKey = EncryptionMachine.encrypt(key, EncryptionMachine.privateWorkFileNameKey, EncryptionMachine.privateWorkFileNameIv);
                File file = new File(userConfigPath +"/cas");
                file.delete();
                EncryptionMachine.createKeyFile(userConfigPath +"/cas", enKey);

                EncryptionMachine.getworkKey(userConfigPath +"/cas", EncryptionMachine.privateWorkFileNameKey, EncryptionMachine.privateWorkFileNameIv);

            } catch (Exception e) {
                log.error("stackTrace {}",e.getStackTrace());
                log.error(e.getMessage());
            }
        }

        if(httpHeaders == null){
            return false;
        }

        return true;

    }

    public void excureCmd(boolean isMaster,Host host){
        log.info("biegin to run bat");
        Properties prop = new Properties();
        try{
            //读取属性文件a.properties
            String PATH = securityFilePath + "/config/config.properties";
            InputStream in = new BufferedInputStream(new FileInputStream(PATH));
            prop.load(in);
            in.close();
        }
        catch(Exception e){
            log.error("stackTrace {}",e.getStackTrace());
            log.error(e.getMessage());
        }

        String home = securityFilePath;
        String cmd = "";
        if(isMaster){
            try {
                statrService();
            }catch (Exception e) {
                log.error("stackTrace {}",e.getStackTrace());
                log.error(e.getMessage());
            }
            String pwd = null;
            try {
                pwd = EncryptionMachine.decrypt(prop.getProperty("enp"), EncryptionMachine.certWorkFileNameKey, EncryptionMachine.MD5_16(prop.getProperty("iv")));
            } catch (Exception e) {
                log.error("stackTrace {}",e.getStackTrace());
                log.error(e.getMessage());
            }
            //主机发布
            cmd = "cmd /c powershell "+home + "/dual/Scripts/Dual_Enable_Master.ps1 " +
                    host.getMasterHostName()+"\\cascadegw "+host.getMasterHostName()+" "+host.getSlaveHostName()+" "+
                    home+"/dual "+ pwd;

        }else{
            host.setRunCmd(true);
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String pwd = null;
            try {
                pwd = EncryptionMachine.decrypt(prop.getProperty("enp"), EncryptionMachine.certWorkFileNameKey, EncryptionMachine.MD5_16(prop.getProperty("iv")));
            } catch (Exception e) {
                log.error("stackTrace {}",e.getStackTrace());
                log.error(e.getMessage());
            }
            //备机订阅
            cmd = "cmd /c powershell "+home + "/dual/Scripts/Dual_Enable_Slave.ps1 " +
                    host.getMasterHostName()+"\\cascadegw "+host.getSlaveHostName()+" "+host.getMasterHostName()+" "+
                    home+"/dual "+pwd;
        }


        try {
            String logString = execUtil.exeCommand(cmd);
            log.info(logString);
        }catch (Exception e){

            log.error("stackTrace {}",e.getStackTrace());
            log.error(e.getMessage());
        }


    }

    private void cancelSlave(Host host){
        if(host.isRunCmd()){
            String home = securityFilePath;
            String cmd = "cmd /c powershell "+home + "/dual/Scripts/Dual_Disable_Slave.ps1 " +
                    host.getMasterHostName()+" "+host.getSlaveHostName()+" "+
                    home+"/dual";
            try {
                log.info(cmd);
                String logString = execUtil.exeCommand(cmd);
                host.setRunCmd(false);
                log.info(logString);
            }catch (Exception e){
                log.error("stackTrace {}",e.getStackTrace());
                log.error(e.getMessage());
            }
        }
    }

    @Override
    public void statrService(){
        log.info("{}init node{}", CoreConfig.splitLine, CoreConfig.splitLine);
        startService.loadNodeByDatabase();

        log.info("{}init license{}", CoreConfig.splitLine, CoreConfig.splitLine);
        try {
            licenseManagerService.initLicense();
        } catch (MyHttpException exception) {
            exception.printStackTrace();
        }

        log.info("{}init internal data{}", CoreConfig.splitLine, CoreConfig.splitLine);
        internalLinkService.start();
        CommonCache.LOAD_STATUS = true;
    }


}

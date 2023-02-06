package com.suntek.vdm.gw.core.service.impl;

import com.suntek.vdm.gw.common.enums.SmcVersionType;
import com.suntek.vdm.gw.common.pojo.SMCDto;
import com.suntek.vdm.gw.common.util.CheckSMCIpAddress;
import com.suntek.vdm.gw.common.util.SystemConfiguration;
import com.suntek.vdm.gw.common.util.security.EncryptionMachine;
import com.suntek.vdm.gw.core.entity.NodeData;
import com.suntek.vdm.gw.core.entity.VmNodeData;
import com.suntek.vdm.gw.core.service.*;
import com.suntek.vdm.gw.core.thread.CheckSMCIpValidThread;
import com.suntek.vdm.gw.smc.service.impl.SmcHttpServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.List;

@Service
@Slf4j
public class StartServiceImpl implements StartService {
    @Autowired
    private NodeDataService nodeDataService;
    @Autowired
    private NodeManageService nodeManageService;
    @Autowired
    private RoutManageService routManageService;
    @Autowired
    private DataSource dataSource;
    @Autowired
    private CheckSMCIpValidThread checkSMCIpValidThread;

    @Autowired
    private WebServiceTemplate webServiceTemplate;

    @Autowired
    private VmNodeDataService vmNodeDataService;


    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${cas.database.name}")
    private String databaseName;
    @Value("${cas.user.config.file_path}")
    private String userConfigPath;


    @Value("${cas.smc.version}")
    private String smcVersion;
    @Value("${cas.smc.address}")
    private String smcAddress;
    @Value("${newPwd}")
    private String newPwd;

    @Value("${cas.database.db_type}")
    private String dbType;

    @Value("${useAdapt}")
    private Boolean useAdapt;


    /**
     * 双机检查数据库是否正常连接
     */
    public boolean checkDataSource() {
        log.info("check data source");
        int time = 0;
        boolean b = false;
        while (time < 50) {
            try {
                Thread.sleep(time * 1000);
                Connection connection = dataSource.getConnection();
                if (dbType != null && dbType.equals("sqlserver")){
                    b = checkSQLServer(connection,time);
                }else {
                    b = checkPGSQL(connection,time);
                }
                connection.close();
                if (b) {
                    break;
                }
            } catch (Exception e) {
                log.error("exception", e);
            }
            time = time == 0 ? 1 : time;
            time *= 2;
            log.error("check data source connection fail,Connect next time:{}", time);
        }
        if (time > 50) {
            log.error("Please check the pgsql");
            return false;
        }
        return true;
    }

    public Boolean checkPGSQL(Connection connection,int time){
        try {
            if (connection.isValid(time)) {
                log.info("check data source connection success");
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public Boolean checkSQLServer(Connection connection,int time){
        //jtds 的connection 没有isValid，因此校验数据库使用直接查询校验
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT 1");
            if (resultSet.next()){
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }


    public void initDatabase() {
        if(!StringUtils.isEmpty(newPwd)){
            try {
                String enPassword = EncryptionMachine.encrypt(newPwd, EncryptionMachine.workFileNameKey, EncryptionMachine.workFileNameIv);
                Runtime.getRuntime().exec("/home/vdc/base/tools/updatepassword.sh gw " + password + " " + enPassword + " cascadegwr/3.0/config/application-top.properties vdc-w &");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(dbType != null && dbType.equals("sqlserver")){
            initSqlServerDB();
            return;
        }
        try {
            username = EncryptionMachine.decrypt(username, EncryptionMachine.workFileNameKey, EncryptionMachine.workFileNameIv);
        } catch (Exception e) {
            log.error("failed to decrypt certificate password error:" + e);
        }
        try {
            password = EncryptionMachine.decrypt(password, EncryptionMachine.workFileNameKey, EncryptionMachine.workFileNameIv);
        } catch (Exception e) {
            log.error("failed to decrypt certificate password error:" + e);
        }

        Connection conn = null;
        Statement statement = null;
        try {
            String initDBUrl = StringUtils.isNotEmpty(url) ? url.replaceAll(databaseName, "postgres") : null;
            log.info("url:{},username:{}", initDBUrl, username);
            conn = DriverManager.getConnection(initDBUrl, username, password);
            statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT u.datname  FROM pg_catalog.pg_database u where u.datname='" + databaseName + "'");
            if (!resultSet.next()) {
                statement.execute("create database " + databaseName + " with CONNECTION LIMIT = 50");
                log.info("localhost SQLServer create DB[{}] success", databaseName);
            } else {
                log.info("DB[{}] already exists");
            }
            conn = DriverManager.getConnection(url, username, password);
            ScriptRunner runner = new ScriptRunner(conn);
            Resources.setCharset(Charset.forName("UTF-8"));
            runner.setLogWriter(null);//设置是否输出日志
            log.info("Execute sql script");
            InputStream stream = getClass().getClassLoader().getResourceAsStream("sql/pgsql.sql");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            runner.runScript(br);
//            String path = this.getClass().getResource("/sql/pgsql.sql").getPath();
//            runner.runScript(new FileReader(path));
            runner.closeConnection();
        } catch (Exception e) {
            log.error("exception", e);
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                log.error("Init database error:{}", e.getMessage());
            }
        }
    }

    public void initSmcConfig() {
        SystemConfiguration.init(userConfigPath, smcVersion, smcAddress);
        String smcIp = SystemConfiguration.getSmcAddress();
        SmcVersionType smcVersionType = SystemConfiguration.getSmcVersion();


        if (SystemConfiguration.smcVersionIsV2() && !useAdapt) {
           SystemConfiguration.setSmcAddress("127.0.0.1:8002");
           SmcHttpServiceImpl.ssl=false;
        }
        SmcHttpServiceImpl.ip = SystemConfiguration.getSmcAddress();
        SmcHttpServiceImpl.version = SystemConfiguration.getSmcVersion();
        //smc3.0通过域名进行热备
        if(SmcVersionType.V3.equals(SystemConfiguration.getSmcVersion()) && SystemConfiguration.isDomain()){
            SMCDto smcDto = new SMCDto();
            smcDto.setAddress(SystemConfiguration.getSmcAddress());
            smcDto.setDomain(SystemConfiguration.getSmcAddress());
            smcDto.setProtocol("https");
            SMCDto smcDto1 = CheckSMCIpAddress.checkSMCIpAddress(smcDto);
            switch (smcDto1.getCode()){
                case 401:
                    SmcHttpServiceImpl.ip = smcDto1.getAddress();
                    checkSMCIpValidThread.startCheck();
                    break;
                default:
                    log.error("checkSmcAddress failed. code: {}", smcDto1.getCode());
            }
        }

        if(useAdapt){
            log.info("==============ip: {}",SmcHttpServiceImpl.ip);
            webServiceTemplate.setDefaultUri("https://"+SmcHttpServiceImpl.ip+"/ws/smcexternal2.asmx");
        }

        NodeData local = nodeDataService.getLocal();
        if (local == null) {
            log.info("Local node not config");
            return;
        }


        if (smcIp == null || smcIp.equals("127.0.0.1")) {
            SystemConfiguration.setSmcAddress(local.getIp());
        } else {
            if ((!smcIp.equals(local.getIp())) || (!smcVersionType.getValue().equals(local.getSmcVersion()))) {
//                local.setIp(smcIp);
                local.setIp(SystemConfiguration.getSmcAddress());
                local.setSmcVersion(smcVersionType.getValue());
                local.setPassword(local.decryptPassword());
                nodeDataService.update(local);
            }
        }
    }


    @Override
    public void loadNodeByDatabase() {
        log.info("Start loading initial data");
        //先初始化路由
        List<NodeData> lows = nodeDataService.getLow();
        for (NodeData item : lows) {
            routManageService.generateRoute(item.toGwNode());
        }
        //初始化虚拟节点路由
        List<VmNodeData> vmLows = vmNodeDataService.getAll();
        for (VmNodeData item : vmLows) {
            routManageService.generateRoute(item.toGwNode());
        }

        //加载本地节点
        NodeData local = nodeDataService.getLocal();
        if (local == null) {
            //TODO 告警
            log.info("Local node not config");
            return;
        }
        nodeManageService.add(local.getId());
        for (VmNodeData item : vmLows) {
            nodeManageService.addVmNode(item.getId());
        }

        //加载其它节点
        List<NodeData> nodeData = nodeDataService.getNotLocal();
        for (NodeData item : nodeData) {
            nodeManageService.add(item.getId());
        }
    }

    public void initSqlServerDB(){
        Connection scriptRunCon = null;
        try {
            scriptRunCon = dataSource.getConnection();
            ScriptRunner runner = new ScriptRunner(scriptRunCon);
            Resources.setCharset(Charset.forName("UTF-8"));
            runner.setLogWriter(null);//设置是否输出日志
            log.info("Execute sqlserver sql script");
            InputStream stream = getClass().getClassLoader().getResourceAsStream("sql/sqlserver.sql");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            runner.runScript(br);
            runner.closeConnection();
        }catch (Exception e){
            log.error(e.getMessage());

        }finally {
            if ( scriptRunCon != null){
                try {
                    scriptRunCon.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        }
    }
}

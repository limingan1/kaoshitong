package com.suntek.vdm.gw.core.config;



import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.incrementer.IKeyGenerator;

import com.baomidou.mybatisplus.extension.incrementer.H2KeyGenerator;


import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.suntek.vdm.gw.common.util.dual.ExecUtil;
import com.suntek.vdm.gw.common.util.security.EncryptionMachine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.*;


@Configuration
@Slf4j
public class MybatisPlusConfig {


    @Value("${spring.datasource.url}")
    private String url;
    @Value("${spring.datasource.driver-class-name}")
    private String driver;
    @Value("${spring.datasource.username}")
    private String username;
    @Value("${spring.datasource.password}")
    private String password;
    @Value("${cas.database.name}")
    private String databaseName;
    @Value("${newPwd}")
    private String newPwd;

    @Value("${cas.database.db_type}")
    private String dbType;

    @Value("${cas.database.dual_computer_enable}")
    private Boolean dualComputerEnable;

    /**
     * sqlServer 单双机数据库连接url以及驱动（临时配置）
     */
    @Value("${spring.datasource.sql_server.single.url}")
    private String sqlServerSingleUrl;
    @Value("${spring.datasource.sql_server.single.driver-class-name}")
    private String sqlServerSingleDriverClassName;

    @Value("${spring.datasource.sql_server.dual.url}")
    private String sqlServerDualUrl;
    @Value("${spring.datasource.sql_server.dual.driver-class-name}")
    private String sqlServerDualDriverClassName;

    @Autowired
    private ExecUtil execUtil;

    /**
     *   mybatis-plus分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paginationInnerInterceptor;
        if (dbType == null || dbType.equals("pgsql")) {
            paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.POSTGRE_SQL);
        } else {
            paginationInnerInterceptor = new PaginationInnerInterceptor(DbType.SQL_SERVER);
        }
        interceptor.addInnerInterceptor(paginationInnerInterceptor);

        return interceptor;
    }
    @Bean
    public IKeyGenerator keyGenerator() {
        return new H2KeyGenerator();
    }

    @Bean
    public DataSource dataSource() throws SQLException {
        if(!StringUtils.isEmpty(newPwd)){
            try {
                String enPassword = EncryptionMachine.encrypt(newPwd, EncryptionMachine.workFileNameKey, EncryptionMachine.workFileNameIv);
                Runtime.getRuntime().exec("/home/vdc/base/tools/updatepassword.sh gw " + password + " " + enPassword + " cascadegwr/3.0/config/application-top.properties vdc-w &");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DruidDataSource druidDataSource = new DruidDataSource();
        //Linux 版本默认使用Pgsql
        if (dbType == null || dbType.equals("pgsql")){
            try {
                username = EncryptionMachine.decrypt(username, EncryptionMachine.workFileNameKey, EncryptionMachine.workFileNameIv);
            } catch (Exception e) {
                log.error("failed to decrypt certificate password error:" + e);
            }
            try {
                password = EncryptionMachine.decrypt(password,  EncryptionMachine.workFileNameKey, EncryptionMachine.workFileNameIv);
            } catch (Exception e) {
                log.error("failed to decrypt certificate password error:" + e);
            }
            druidDataSource.setUrl(url);
            druidDataSource.setDriverClassName(driver);
            druidDataSource.setUsername(username);
            druidDataSource.setPassword(password);
        }else {
            String connectUrl = null;
            String driverClassName = null;
            //Windows 版本加载SQLServer驱动，分为单双机，区别标志为变量dualComputerEnable
            if (dualComputerEnable != null && dualComputerEnable){
                connectUrl = sqlServerDualUrl;
                driverClassName = sqlServerDualDriverClassName;
            }else {
                connectUrl = sqlServerSingleUrl;
                driverClassName = sqlServerSingleDriverClassName;
            }

            createSQLServerDB(connectUrl);
            druidDataSource.setUrl(connectUrl);
            druidDataSource.setDriverClassName(driverClassName);
            druidDataSource.setUsername("");
            druidDataSource.setPassword("");
//            druidDataSource.setValidationQuery("SELECT 1");
        }

        return druidDataSource;
    }

    //连接数据库连接池前先检测是否有数据库，避免连接时因为没有数据库而导致连接不上的问题
    public void createSQLServerDB(String url){
        String sign = System.getProperty("GW_FLAG");
        String check = "check";
        if (check.equals(sign)) {
            String initDBUrl = StringUtils.isNotEmpty(url) ? url.replaceAll(databaseName, "master") : null;
            Connection conn = null;
            Statement stat = null;
            String className = null;
            if (dualComputerEnable) {
                className = sqlServerDualDriverClassName;
            } else {
                className = sqlServerSingleDriverClassName;
            }
            try {
                Class.forName(className);
                conn = DriverManager.getConnection(initDBUrl);
                stat = conn.createStatement();
                ResultSet resultSet = stat.executeQuery("select * From sysdatabases where name='" + databaseName + "'");

                if (!resultSet.next()) {
                    String sql = "if not EXISTS (select * From sysdatabases where name='" + databaseName + "') create database " + databaseName + " COLLATE Chinese_PRC_CI_AS";
                    int f = stat.executeUpdate(sql);
                    if (f >= 0) {
                        log.info("localhost SQLServer create DB[" + databaseName + "] success.");
                    }
                } else {
                    log.info("localhost SQLServer exist DB[" + databaseName + "] before.");
                }
                String authLocal = "db_owner";
                String roleLocal = "NT AUTHORITY\\SYSTEM";

                String sql = "use vdmcas3;exec sp_addrolemember '" + authLocal + "', '" + roleLocal + "';";
                int r = stat.executeUpdate(sql);
                log.info("sp_addrolemember"+r);
                if (r >= 0) {
                    log.info("localhost SQLServer exec db_owner to NT AUTHORITY\\SYSTEM");
                }
                stat.close();
                conn.close();
            } catch (Exception e) {
                log.error(e.getMessage());
                log.error("init dataBase error: {}", e.getStackTrace());
            } finally {
                if (null != stat) {
                    try {
                        stat.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                if (null != conn) {
                    try {
                        conn.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }

//            Connection conn = null;
//            Statement stat = null;

            try {
                conn = DriverManager.getConnection(url);
                stat = conn.createStatement();
                stat.close();
                conn.close();
            } catch (SQLException throwables) {
                log.error(throwables.getMessage());
                log.error("SQLException:{}",throwables.getStackTrace());
                throwables.printStackTrace();
            }finally {
                if (null != stat) {
                    try {
                        stat.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
                if (null != conn) {
                    try {
                        conn.close();
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
            }



            try {
                execUtil.openBat("\\tools\\shutdown.bat");
                System.out.println("init finish");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println(e);
            }
            System.exit(1);

        }


//
//        String connetUrl = url.replace(databaseName,"master");
//        Connection connection = null;
//        try {
//            Class.forName(className);
//            connection = DriverManager.getConnection(connetUrl);
//            Statement statement = connection.createStatement();
//            ResultSet resultSet = statement.executeQuery("SELECT * From sysdatabases where name = '" + databaseName + "'");
//            if (!resultSet.next()) {
//                statement.execute("create database " + databaseName + " COLLATE Chinese_PRC_CI_AS");
//                log.info("localhost SQLServer create DB[{}] success", databaseName);
//            } else {
//                log.info("DB[{}] already exists",databaseName);
//            }
//        }catch (Exception e){
//            log.error("create sqlserver db fail when init datasource.catch error:{}",e);
//        }finally {
//            if (connection != null){
//                try {
//                    connection.close();
//                } catch (SQLException throwables) {
//                    throwables.printStackTrace();
//                }
//            }
//        }

    }
}

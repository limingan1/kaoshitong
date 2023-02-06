package com.suntek.vdm.gw.core.config;

import com.suntek.vdm.gw.common.util.security.EncryptionMachine;
import com.suntek.vdm.gw.common.pojo.CoreConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Slf4j
@Configuration
public class HttpConfig {
    @Value("${server.http-port}")
    private Integer port;

    @Value("${server.port}")
    private Integer httpsPort;

    @Value("${cas.security.file_path}")
    private String securityFilePath;

    @Value("${cas.database.db_type}")
    private String db_type;

    //https  开启同时开启http
    @Bean
    public ServletWebServerFactory servletContainer() {
        log.info("{}config Https{}", CoreConfig.splitLine, CoreConfig.splitLine);

        if (securityFilePath.contains("classpath:")) {
            String relativePath = securityFilePath.replace("classpath:", "");
            String classPath = System.getProperties().getProperty("user.dir");
            securityFilePath = classPath + relativePath;
        }

        String path = securityFilePath;
        String certPath = securityFilePath;
        if("sqlserver".equals(db_type)){
            path += "/tools";
            certPath += "/tools";
        }else{
            path += "/image";
            certPath += "/cert";
        }
        EncryptionMachine.init(path, db_type);
        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {
//            @Override
//            protected void postProcessContext(Context context) {
//                // 如果要强制使用https，请松开以下注释
//                // SecurityConstraint constraint = new SecurityConstraint();
//                // constraint.setUserConstraint("CONFIDENTIAL");
//                // SecurityCollection collection = new SecurityCollection();
//                // collection.addPattern("/*");
//                // constraint.addCollection(collection);
//                // context.addConstraint(constraint);
//            }
        };
        String vdcPassword = EncryptionMachine.readFile(certPath + "/vdc");
        String certPassword = null;
        try {
            certPassword = EncryptionMachine.decrypt(vdcPassword, EncryptionMachine.certWorkFileNameKey, EncryptionMachine.certWorkFileNameIv);
        } catch (Exception e) {
            log.error("failed to decrypt certificate password error:" + e);
        }
        Ssl ssl = new Ssl();
        ssl.setKeyStore(certPath + "/vdc.jks");
        ssl.setKeyStorePassword(certPassword);
        ssl.setKeyPassword(certPassword);
//        ssl.setKeyStoreType("JKS");
//        ssl.setKeyAlias("tomcat");
        tomcat.setSsl(ssl);
        tomcat.addAdditionalTomcatConnectors(createStandardConnector()); // 添加http
        return tomcat;
    }

    // 配置http
    private Connector createStandardConnector() {
        // 默认协议为org.apache.coyote.http11.Http11NioProtocol
        Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
        connector.setSecure(false);
        connector.setScheme("http");
        connector.setPort(port);
        connector.setRedirectPort(httpsPort); // 当http重定向到https时的https端口号
        return connector;
    }
}
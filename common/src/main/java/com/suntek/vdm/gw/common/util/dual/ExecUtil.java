package com.suntek.vdm.gw.common.util.dual;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;

@Slf4j
@Service
public class ExecUtil {

    @Value("${cas.security.file_path}")
    private String securityFilePath;

    private static final String DEFAULT_CHARSET = "GBK";
    private static final Long TIMEOUT = 10000L;

    /**
     * 执行指定命令
     *
     * @param command 命令
     * @return 命令执行完成返回结果
     * @throws RuntimeException 失败时抛出异常，由调用者捕获处理
     */
    public synchronized String exeCommand(String command) throws RuntimeException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            log.info("cmd: {}",command);
            int exitCode = exeCommand(command, out);
            if (exitCode == 0) {
                log.info("command run success: " + System.currentTimeMillis());
            } else {
                log.info("command run fail: " + System.currentTimeMillis());
            }
            return out.toString(DEFAULT_CHARSET);
        } catch (Exception e) {
            log.info(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * 执行指定命令，输出结果到指定输出流中
     *
     * @param command 命令
     * @param out     执行结果输出流
     * @return 执行结果状态码：执行成功返回0
     * @throws ExecuteException 失败时抛出异常，由调用者捕获处理
     * @throws IOException      失败时抛出异常，由调用者捕获处理
     */
    public synchronized int exeCommand(String command, OutputStream out) throws ExecuteException, IOException {
        CommandLine commandLine = CommandLine.parse(command);
        PumpStreamHandler pumpStreamHandler = null;
        if (null == out) {
            pumpStreamHandler = new PumpStreamHandler();
        } else {
            pumpStreamHandler = new PumpStreamHandler(out);
        }
        // 设置超时时间为10秒
        ExecuteWatchdog watchdog = new ExecuteWatchdog(TIMEOUT);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setStreamHandler(pumpStreamHandler);
        executor.setWatchdog(watchdog);
        return executor.execute(commandLine);
    }

    public void openBat(String path){
        // 执行批处理文件
        String bat = securityFilePath+path;
        System.out.println("run bat: "+bat);
        Runtime rt = Runtime.getRuntime();
        Process ps = null;
        StringBuilder sb = new StringBuilder();
        try {
            ProcessBuilder builder = new ProcessBuilder(bat);
            builder.redirectErrorStream(true);
            ps = builder.start();
            InputStream in = ps.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(in));
            String line;
            while((line=bufferedReader.readLine())!=null)
            {
                sb.append(line + "\n");
            }
            try {
                ps.waitFor();
            } catch (InterruptedException e) {
                System.out.println(e);
            }
            System.out.println("sb:" + sb.toString());
            System.out.println("callCmd execute finished");
            in.close();
            ps.destroy();
            ps = null;
        } catch (Exception e) {
            System.out.println("调用"+bat+"失败");
        }
    }
}

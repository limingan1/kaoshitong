package com.suntek.vdm.gw.api.controller.core;

import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/log")
public class LogController {

    @Autowired
    private HttpServletResponse response;
    private static void downloadZip(OutputStream outputStream, File[] fileList){
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(outputStream);
            for (File file : fileList) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOutputStream.putNextEntry(zipEntry);
                byte[] buf = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(file);
                while ((len = in.read(buf)) != -1) {
                    zipOutputStream.write(buf, 0, len);
                    zipOutputStream.flush();
                }
            }
            zipOutputStream.flush();
            zipOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            try {
                if (zipOutputStream != null ) {
                    zipOutputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @PostMapping("/download")
    public void downloadCasLog(@RequestParam(value = "date",required = false) String date) throws IOException {
        String fileName = date == null ? "cascadegwr.log.zip" : "cascade_" + date + ".zip";
        response.setHeader("content-type", "application/octet-stream");
        response.setContentType("application/octet-stream");
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
        response.setHeader("Content-Disposition", "attachment;filename="
                + new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        String datePath = date == null ? "" : "/" + date;
        String path = getPath() + "/logs" + datePath;
        if (date == null) {
            String pathCascade = path + "/cascade.log";
            String pathMessage = path + "/cascade-message.log";
            File[] fileList = {new File(pathCascade), new File(pathMessage)};
            downloadZip(response.getOutputStream(), fileList);
        } else {
            File zipFile = new File(path);
            downloadZip(response.getOutputStream(), Objects.requireNonNull(zipFile.listFiles()));
        }
    }

    public String getPath() {
        String path = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        if (System.getProperty("os.name").contains("dows")) {
            path = path.substring(1);
        }
        if(path.contains("jar")){
            path = path.substring(0,path.lastIndexOf("."));
            return path.replace("/cascadegwr3.0", "");
        }
        return path.replace("cascadegwr3.0/api/target/classes/", "");
    }

    @GetMapping("/dir")
    public ResponseEntity<String> getLogDir() {
        String path = getPath();
        File file = new File(path + "/logs");
        File[] files = file.listFiles();
        if (files == null) {
            return new ResponseEntity<>("[]", HttpStatus.OK);
        }
        List<String> dirList = new ArrayList<>();
        for (File dir : files) {
            if (dir.isDirectory()) {
                dirList.add(dir.getName());
            }
        }
        dirList = dirList.stream().sorted(Comparator.reverseOrder()).collect(Collectors.toList());
        return new ResponseEntity<>(JSONObject.toJSONString(dirList), HttpStatus.OK);
    }

}

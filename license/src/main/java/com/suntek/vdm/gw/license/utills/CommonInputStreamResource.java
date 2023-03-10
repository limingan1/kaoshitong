package com.suntek.vdm.gw.license.utills;

import org.springframework.core.io.InputStreamResource;

import java.io.InputStream;

public class CommonInputStreamResource extends InputStreamResource {
    private long length;
    private String fileName;

    public CommonInputStreamResource(InputStream inputStream, long length, String fileName) {
        super(inputStream);
        this.length = length;
        this.fileName = fileName;
    }

    /**
     * 覆写父类方法
     * 如果不重写这个方法，并且文件有一定大小，那么服务端会出现异常
     * {@code The multi-part request contained parameter data (excluding uploaded files) that exceeded}
     */
    @Override
    public String getFilename() {
        return fileName;
    }

    /**
     * 覆写父类 contentLength 方法
     * 因为 {@link org.springframework.core.io.AbstractResource#contentLength()}方法会重新读取一遍文件，
     * 而上传文件时，restTemplate 会通过这个方法获取大小。然后当真正需要读取内容的时候，发现已经读完，会报如下错误。
     * <code>
     * java.lang.IllegalStateException: InputStream has already been read - do not use InputStreamResource if a stream needs to be read multiple times
     * at org.springframework.core.io.InputStreamResource.getInputStream(InputStreamResource.java:96)
     * </code>
     * <p>
     * ref:com.amazonaws.services.s3.model.S3ObjectInputStream#available()
     */
    @Override
    public long contentLength() {
        long estimate = length;
        return estimate == 0 ? 1 : estimate;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}


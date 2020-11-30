package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.exception.MRestFileUploadException;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.*;
import java.util.UUID;

/**
 * @author jiashunx
 */
public class MRestFileUpload {

    /**
     * 上传文件的Content-Type.
     */
    private final String contentType;
    /**
     * 上传文件的文件名.
     */
    private final String filename;
    /**
     * 上传文件的Content-Length.
     */
    private final long contentLength;
    /**
     * 文件在当前服务器对应的磁盘文件.
     */
    private File file;

    public MRestFileUpload(FileUpload fileUpload) {
        try {
            this.contentType = fileUpload.getContentType();
            this.filename = fileUpload.getFilename();
            this.contentLength = fileUpload.length();
            File tmpFile = null;
            // 将内存数据持久化到磁盘
            if (fileUpload.isInMemory()) {
                // 创建临时文件.
                tmpFile = new File(MRestUtils.getSystemTempDirPath() + "mr_" + UUID.randomUUID().toString());
                ByteArrayInputStream inputStream = new ByteArrayInputStream(fileUpload.get());
                FileOutputStream outputStream = null;
                try {
                    tmpFile.createNewFile();
                    outputStream = new FileOutputStream(tmpFile);
                    byte[] buffer = new byte[2048];
                    int readSize = 0;
                    while ((readSize = inputStream.read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, readSize);
                    }
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    inputStream.close();
                }
            } else {
                // 磁盘文件拷贝
                tmpFile = new File(fileUpload.getFile().getAbsolutePath() + "_mr");
                FileInputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    tmpFile.createNewFile();
                    inputStream = new FileInputStream(fileUpload.getFile());
                    outputStream = new FileOutputStream(tmpFile);
                    byte[] buffer = new byte[2048];
                    int readSize = 0;
                    while ((readSize = inputStream.read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, readSize);
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
            this.file = tmpFile;
        } catch (Throwable throwable) {
            throw new MRestFileUploadException("crete fileupload object failed.", throwable);
        }
    }

    public String getContentType() {
        return contentType;
    }

    public String getFilename() {
        return filename;
    }

    public long getContentLength() {
        return contentLength;
    }

    public File getFile() {
        return file;
    }

    public String getFilePath() {
        return getFile().getAbsolutePath();
    }

    public File getFileDirectory() {
        return getFile().getParentFile();
    }

}

package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.exception.MRestFileUploadException;
import io.github.jiashunx.masker.rest.framework.util.FileUtils;
import io.github.jiashunx.masker.rest.framework.util.IOUtils;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.UUID;

/**
 * @author jiashunx
 */
public class MRestFileUpload {

    private static final Logger logger = LoggerFactory.getLogger(MRestFileUpload.class);

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
                String filePath = MRestUtils.getSystemTempDirPath() + "mr_" + UUID.randomUUID().toString();
                tmpFile = FileUtils.newFile(filePath);
                if (logger.isDebugEnabled()) {
                    logger.debug("store temp file from memory, target: {}", tmpFile);
                }
                IOUtils.write(fileUpload.get(), tmpFile);
            } else {
                // 磁盘文件拷贝
                File sourceFile = fileUpload.getFile();
                String filePath = sourceFile.getAbsolutePath() + "_mr";
                tmpFile = FileUtils.newFile(filePath);
                if (logger.isDebugEnabled()) {
                    logger.debug("store temp file, source: {}, target: {}", sourceFile, tmpFile);
                }
                IOUtils.copy(sourceFile, tmpFile);
            }
            this.file = tmpFile;
        } catch (Throwable throwable) {
            throw new MRestFileUploadException("create fileupload object failed.", throwable);
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

    public InputStream getFileInputStream() {
        try {
            return new FileInputStream(this.file);
        } catch (FileNotFoundException e) {}
        return null;
    }

    public void copyFile(File newFile) throws IOException {
        FileUtils.newFile(newFile.getAbsolutePath());
        IOUtils.copy(getFileInputStream(), new FileOutputStream(newFile));
    }

    public synchronized void release() {
        if (this.file != null) {
            FileUtils.deleteFile(this.file);
            this.file = null;
        }
    }

}

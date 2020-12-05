package io.github.jiashunx.masker.rest.framework.model;

/**
 * disk file resource model.
 * @author jiashunx
 */
public class DiskFileResource {

    private String url;
    private String diskFilePath;
    private byte[] bytes;
    private String contentType;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDiskFilePath() {
        return diskFilePath;
    }

    public void setDiskFilePath(String diskFilePath) {
        this.diskFilePath = diskFilePath;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}

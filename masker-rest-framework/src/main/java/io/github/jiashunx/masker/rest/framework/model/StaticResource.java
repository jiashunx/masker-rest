package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.type.StaticResourceType;

import javax.activation.MimetypesFileTypeMap;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class StaticResource {

    /**
     * resource type.
     */
    private final StaticResourceType type;
    /**
     * resource uri.
     */
    private final String uri;
    /**
     * 静态资源路径.
     */
    private String url;
    /**
     * bytes of content.
     */
    private final byte[] contents;
    /**
     * file name.
     */
    private final String fileName;
    /**
     * Content-Type.
     */
    private final String contentType;

    public StaticResource(StaticResourceType type, String uri, String url, byte[] contents) {
        this.type = Objects.requireNonNull(type);
        this.uri = Objects.requireNonNull(uri);
        this.url = Objects.requireNonNull(url);
        this.contents = Objects.requireNonNull(contents);
        this.fileName = url.substring(url.lastIndexOf(Constants.URL_PATH_SEP) + 1);
        this.contentType = new MimetypesFileTypeMap().getContentType(this.fileName);
    }

    public StaticResourceType getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public byte[] getContents() {
        return contents;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }
}

package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.cons.Constants;

import javax.activation.MimetypesFileTypeMap;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class StaticResource {

    /**
     * resource uri.
     */
    private final String uri;
    /**
     * 静态资源路径.
     */
    private final String url;
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

    public StaticResource(String uri, String url, byte[] contents) {
        this.uri = Objects.requireNonNull(uri);
        this.url = Objects.requireNonNull(url);
        this.contents = Objects.requireNonNull(contents);
        this.fileName = url.substring(url.lastIndexOf(Constants.URL_PATH_SEP) + 1);
        this.contentType = new MimetypesFileTypeMap().getContentType(this.fileName);
    }

    public String getUri() {
        return uri;
    }

    public String getUrl() {
        return url;
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

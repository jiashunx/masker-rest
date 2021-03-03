package io.github.jiashunx.masker.rest.framework.model;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class UrlPathModel {

    protected final String path;
    protected final String pathVal;
    protected final boolean root;

    public UrlPathModel(String p) {
        path = Objects.requireNonNull(p).trim();
        if (!path.startsWith("/") || path.indexOf("/") != path.lastIndexOf("/")) {
            throw new IllegalArgumentException();
        }
        root = path.length() == 1;
        pathVal = path.substring(1);
    }

    public String getPath() {
        return path;
    }

    public String getPathVal() {
        return pathVal;
    }

    public boolean isRoot() {
        return root;
    }
}

package io.github.jiashunx.masker.rest.framework.model;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class UrlPathModel {

    protected String path;
    protected String pathVal;
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

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UrlPathModel that = (UrlPathModel) object;
        return root == that.root &&
                Objects.equals(path, that.path) &&
                Objects.equals(pathVal, that.pathVal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, pathVal, root);
    }
}

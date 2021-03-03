package io.github.jiashunx.masker.rest.framework.model;

import java.util.List;

/**
 * @author jiashunx
 */
public class UrlModel {

    private final String url;
    private List<String> pathList;
    private List<String> pathValList;

    public UrlModel(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public List<String> getPathList() {
        return pathList;
    }

    public void setPathList(List<String> pathList) {
        this.pathList = pathList;
    }

    public List<String> getPathValList() {
        return pathValList;
    }

    public void setPathValList(List<String> pathValList) {
        this.pathValList = pathValList;
    }
}

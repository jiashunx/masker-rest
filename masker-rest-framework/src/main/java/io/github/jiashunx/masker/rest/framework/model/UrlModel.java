package io.github.jiashunx.masker.rest.framework.model;

import java.util.List;

/**
 * @author jiashunx
 */
public class UrlModel {

    private final String url;
    private List<UrlPathModel> pathList;

    public UrlModel(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public List<UrlPathModel> getPathList() {
        return pathList;
    }

    public void setPathList(List<UrlPathModel> pathList) {
        this.pathList = pathList;
    }

}

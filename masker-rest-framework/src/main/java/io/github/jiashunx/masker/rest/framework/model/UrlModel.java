package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.util.UrlParaser;

import java.util.List;

/**
 * @author jiashunx
 */
public class UrlModel {

    private final String url;
    private final List<UrlPathModel> pathModelList;
    private final int pathModelListSize;
    private final UrlPathModel firstPathModel;
    private final UrlPathModel lastPathModel;

    public UrlModel(String url) {
        this.url = UrlParaser.getUrl(url);
        this.pathModelList = UrlParaser.getUrlPathModelList(url);
        this.pathModelListSize = pathModelList.size();
        this.firstPathModel = this.pathModelList.get(0);
        this.lastPathModel = this.pathModelList.get(pathModelListSize - 1);
    }

    public String getUrl() {
        return url;
    }

    public List<UrlPathModel> getPathModelList() {
        return pathModelList;
    }

    public int getPathModelListSize() {
        return pathModelListSize;
    }

    public UrlPathModel getFirstPathModel() {
        return firstPathModel;
    }

    public UrlPathModel getLastPathModel() {
        return lastPathModel;
    }

    public boolean isRoot() {
        return pathModelList.get(0).isRoot();
    }

}

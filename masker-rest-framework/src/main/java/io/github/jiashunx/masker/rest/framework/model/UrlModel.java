package io.github.jiashunx.masker.rest.framework.model;

import io.github.jiashunx.masker.rest.framework.util.UrlParaser;

import java.util.List;

/**
 * @author jiashunx
 */
public class UrlModel {

    private final String url;
    private final List<UrlPathModel> pathModelList;

    public UrlModel(String url) {
        this.url = UrlParaser.getUrl(url);
        this.pathModelList = UrlParaser.getUrlPathModelList(url);
    }

    public String getUrl() {
        return url;
    }

    public boolean isRoot() {
        return pathModelList.get(0).isRoot();
    }

}

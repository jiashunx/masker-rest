package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.github.jiashunx.masker.rest.framework.util.StaticResourceHolder;

import java.util.*;

/**
 * @author jiashunx
 */
public class StaticResourceFilter implements MRestFilter {

    private final MRestContext restContext;
    private final StaticResourceHolder staticResourceHolder;

    public StaticResourceFilter(MRestContext restContext) {
        this.restContext = Objects.requireNonNull(restContext);
        this.staticResourceHolder = new StaticResourceHolder(this.restContext);
    }

    public void reloadClasspathResource(Map<String, List<String>> pathMap) {
        this.staticResourceHolder.reloadClasspathResourceMap(pathMap);
    }

    public void reloadDiskResource(Map<String, List<String>> pathMap) {
        this.staticResourceHolder.reloadDiskResourceMap(pathMap);
    }

    @Override
    public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
        String requestUrl = restRequest.getUrl();
        Map<String, StaticResource> resourceMap = staticResourceHolder.getResourceMap();
        StaticResource resource = resourceMap.get(requestUrl);
        if (resource != null) {
            byte[] bytes = resource.getContents();
            String contentType = restRequest.getAcceptFirst();
            if (contentType == null) {
                contentType = resource.getContentType();
            }
            restResponse.write(bytes, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, contentType));
            return;
        }
        filterChain.doFilter(restRequest, restResponse);
    }

}

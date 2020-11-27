package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.github.jiashunx.masker.rest.framework.util.StaticResourceHolder;

import java.util.Map;

/**
 * @author jiashunx
 */
public class StaticResourceFilter implements MRestFilter {

    @Override
    public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
        String requestUrl = restRequest.getUrl();
        Map<String, StaticResource> resourceMap = StaticResourceHolder.getResourceMap();
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

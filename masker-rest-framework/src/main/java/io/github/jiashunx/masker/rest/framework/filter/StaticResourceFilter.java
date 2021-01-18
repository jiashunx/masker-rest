package io.github.jiashunx.masker.rest.framework.filter;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import io.github.jiashunx.masker.rest.framework.serialize.MRestSerializer;
import io.github.jiashunx.masker.rest.framework.util.IOUtils;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.github.jiashunx.masker.rest.framework.util.StaticResourceHolder;

import java.util.*;

/**
 * @author jiashunx
 */
public class StaticResourceFilter implements MRestFilter {

    private final MRestContext restContext;

    private static final String DEFAULT_CONTENT_TYPE_KEY = ".*";
    private static final Map<String, String> CONTENT_TYPE_MAP = new HashMap<>();
    static {
        String json = IOUtils.loadContentFromClasspath("masker-rest/content-type.json");
        Map<?, ?> map = MRestSerializer.jsonToObj(json, Map.class);
        map.forEach((key, value) -> {
            CONTENT_TYPE_MAP.put(String.valueOf(key), String.valueOf(value));
        });
    }

    public StaticResourceFilter(MRestContext restContext) {
        this.restContext = Objects.requireNonNull(restContext);
    }

    @Override
    public void doFilter(MRestRequest restRequest, MRestResponse restResponse, MRestFilterChain filterChain) {
        String requestUrl = restRequest.getUrl();
        Map<String, StaticResource> resourceMap = restContext.getStaticResourceHolder().getResourceMap();
        StaticResource resource = resourceMap.get(requestUrl);
        if (resource != null) {
            byte[] bytes = resource.getContents();
            String contentType = restRequest.getAcceptFirst();
            if (contentType == null) {
                contentType = resource.getContentType();
            }
            String suffix = DEFAULT_CONTENT_TYPE_KEY;
            int index = requestUrl.lastIndexOf(".");
            if (index > 0) {
                suffix = requestUrl.substring(index);
            }
            String _contentType = CONTENT_TYPE_MAP.get(suffix);
            if (_contentType != null) {
                contentType = _contentType;
            }
            restResponse.write(bytes, MRestHeaderBuilder.Build(Constants.HTTP_HEADER_CONTENT_TYPE, contentType));
            return;
        }
        filterChain.doFilter(restRequest, restResponse);
    }

}

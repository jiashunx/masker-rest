package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author jiashunx
 */
public final class StaticResourceHolder {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceHolder.class);

    private StaticResourceHolder() {}

    private static final Map<String, StaticResource> STATIC_RESOURCE_MAP = new HashMap<>();
    static {
        // 从上到下优先级依次从高到低.
        List<StaticResource> resourceList = new ArrayList<>();
        resourceList.addAll(getResources("META-INF/resources/"));
        resourceList.addAll(getResources("resources/"));
        resourceList.addAll(getResources("static/"));
        resourceList.addAll(getResources("public/"));
        for (StaticResource resource: resourceList) {
            String url = resource.getUrl();
            if (STATIC_RESOURCE_MAP.containsKey(url)) {
                continue;
            }
            STATIC_RESOURCE_MAP.put(url, resource);
        }
        if (logger.isInfoEnabled()) {
            STATIC_RESOURCE_MAP.forEach((key, resource) -> {
                logger.info("load static resource: {}", key);
            });
        }
    }

    public static Map<String, StaticResource> getResourceMap() {
        Map<String, StaticResource> map = new HashMap<>();
        STATIC_RESOURCE_MAP.forEach(map::put);
        return map;
    }

    public static List<StaticResource> getResources(String cpDirLocation) {
        List<StaticResource> resourceList = new ArrayList<>();
        try {
            if (StringUtils.isBlank(cpDirLocation)) {
                throw new IllegalArgumentException("classpath directory location can't be empty");
            }
            String location = cpDirLocation.trim();
            if (!location.endsWith(Constants.URL_PATH_SEP)) {
                location = location + Constants.URL_PATH_SEP;
            }
            while (location.startsWith(Constants.URL_PATH_SEP)) {
                if (location.length() == 1) {
                    break;
                }
                location = location.substring(1);
            }
            int urlPrefixLen = location.length();
            String _location = String.format("classpath*:%s**", location);
            if (logger.isInfoEnabled()) {
                logger.info("get static resources from classpath location: {}", _location);
            }
            Resource[] resources = new PathMatchingResourcePatternResolver().getResources(_location);
            for (Resource resource: resources) {
                String uri = resource.getURI().toString();
                // 排除目录
                if (uri.endsWith(Constants.URL_PATH_SEP)) {
                    continue;
                }
                int lastIdx = uri.lastIndexOf(location);
                String url = uri.substring(lastIdx + urlPrefixLen);
                if (!url.startsWith(Constants.URL_PATH_SEP)) {
                    url = Constants.URL_PATH_SEP + url;
                }
                byte[] contentBytes = null;
                try(InputStream inputStream = resource.getInputStream()) {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(inputStream.available());
                    byte[] buffer = new byte[1024];
                    int readSize = 0;
                    while ((readSize = inputStream.read(buffer)) >= 0) {
                        outputStream.write(buffer, 0, readSize);
                    }
                    contentBytes = outputStream.toByteArray();
                } catch (Throwable throwable) {
                    if (logger.isErrorEnabled()) {
                        logger.error("load static resource failed: {}", uri, throwable);
                    }
                } finally {
                    if (contentBytes == null) {
                        contentBytes = new byte[0];
                    }
                }
                resourceList.add(new StaticResource(uri, url, contentBytes));
            }
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("get resources from classpath directory location: [{}] failed.", cpDirLocation, throwable);
            }
        }
        return resourceList;
    }

}

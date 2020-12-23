package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.*;

/**
 * @author jiashunx
 */
public final class StaticResourceHolder {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceHolder.class);

    private final MRestContext restContext;

    private volatile Map<String, StaticResource> classpathResourceMap = new HashMap<>();
    private volatile Map<String, StaticResource> diskResourceMap = new HashMap<>();
    private volatile Map<String, StaticResource> resourceMap = new HashMap<>();

    public StaticResourceHolder(MRestContext restContext) {
        this.restContext = Objects.requireNonNull(restContext);
        reloadClasspathResourceMap(Collections.emptyList());
    }

    public Map<String, StaticResource> getResourceMap() {
        return resourceMap;
    }

    public Map<String, StaticResource> getClasspathResourceMap() {
        return classpathResourceMap;
    }

    public Map<String, StaticResource> getDiskResourceMap() {
        return diskResourceMap;
    }

    private synchronized void mergeResourceMap() {
        Map<String, StaticResource> resourceMap = new HashMap<>();
        resourceMap.putAll(diskResourceMap);
        resourceMap.putAll(classpathResourceMap);
        this.resourceMap = resourceMap;
    }

    public synchronized void reloadDiskResourceMap(List<String> pathList) {
        this.diskResourceMap = new HashMap<>();
        mergeResourceMap();
    }

    public synchronized void reloadClasspathResourceMap(List<String> pathList) {
        Map<String, StaticResource> resourceMap = new HashMap<>();
        // 从上到下优先级依次从高到低.
        List<StaticResource> resourceList = new ArrayList<>();
        if (pathList != null && !pathList.isEmpty()) {
            pathList.forEach(cpDirLocation -> {
                resourceList.addAll(getClasspathResources(cpDirLocation));
            });
        }
        for (StaticResource resource: resourceList) {
            String url = resource.getUrl();
            if (resourceMap.containsKey(url)) {
                continue;
            }
            resourceMap.put(url, resource);
        }
        if (logger.isInfoEnabled()) {
            resourceMap.forEach((key, resource) -> {
                logger.info("Context[{}] load static resource: {}", restContext.getContextPath(), key);
            });
        }
        this.classpathResourceMap = resourceMap;
        mergeResourceMap();
    }

    public static List<StaticResource> getClasspathResources(String cpDirLocation) {
        List<StaticResource> resourceList = new ArrayList<>();
        try {
            if (StringUtils.isBlank(cpDirLocation)) {
                throw new IllegalArgumentException("classpath directory location can't be empty");
            }
            String location = cpDirLocation.trim();
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
                    contentBytes = IOUtils.readBytes(inputStream);
                    if (contentBytes == null) {
                        throw new NullPointerException();
                    }
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

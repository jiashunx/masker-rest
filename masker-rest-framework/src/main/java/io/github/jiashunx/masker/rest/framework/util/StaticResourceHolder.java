package io.github.jiashunx.masker.rest.framework.util;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.model.DiskFileResource;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import io.github.jiashunx.masker.rest.framework.type.StaticResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.InputStream;
import java.util.*;

/**
 * @author jiashunx
 */
public final class StaticResourceHolder {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceHolder.class);

    private final MRestContext restContext;

    private volatile Map<String, StaticResource> resourceMap = new HashMap<>();

    public StaticResourceHolder(MRestContext restContext) {
        this.restContext = Objects.requireNonNull(restContext);
        reloadClasspathResourceMap(Collections.emptyMap());
    }

    public Map<String, StaticResource> getResourceMap() {
        return resourceMap;
    }

    public StaticResource getResource(String requestUrl) {
        return resourceMap.get(requestUrl);
    }

    public synchronized void reloadResourceMap(Map<String, List<String>> pathMap0, Map<String, List<String>> pathMap1) {
        Map<String, StaticResource> resourceMap = new HashMap<>();
        resourceMap.putAll(reloadDiskResourceMap(pathMap1));
        // classpath文件优先级高于磁盘文件优先级
        resourceMap.putAll(reloadClasspathResourceMap(pathMap0));
        this.resourceMap = resourceMap;
    }

    public Map<String, StaticResource> reloadDiskResourceMap(Map<String, List<String>> pathMap) {
        // 从上到下优先级依次从高到低.
        List<StaticResource> $resourceList = new ArrayList<>();
        pathMap.forEach((prefixUrl, pathList) -> {
            if (pathList != null && !pathList.isEmpty()) {
                pathList.forEach(diskDirPath -> {
                    $resourceList.addAll(getDiskResources(prefixUrl, diskDirPath));
                });
            }
        });
        Map<String, StaticResource> $resourceMap = resourceListToMap($resourceList);
        if (logger.isDebugEnabled()) {
            $resourceMap.forEach((key, resource) -> {
                logger.debug("{} load static resource from disk: {}", restContext.getContextDesc(), resource.getUrl());
            });
        }
        return $resourceMap;
    }

    public Map<String, StaticResource> reloadClasspathResourceMap(Map<String, List<String>> pathMap) {
        // 从上到下优先级依次从高到低.
        List<StaticResource> $resourceList = new ArrayList<>();
        pathMap.forEach((prefixUrl, pathList) -> {
            if (pathList != null && !pathList.isEmpty()) {
                pathList.forEach(cpDirLocation -> {
                    $resourceList.addAll(getClasspathResources(prefixUrl, cpDirLocation));
                });
            }
        });
        Map<String, StaticResource> $resourceMap = resourceListToMap($resourceList);
        if (logger.isDebugEnabled()) {
            $resourceMap.forEach((key, resource) -> {
                logger.debug("{} load static resource from classpath: {}", restContext.getContextDesc(), resource.getUrl());
            });
        }
        return $resourceMap;
    }

    private static Map<String, StaticResource> resourceListToMap(List<StaticResource> $resourceList) {
        Map<String, StaticResource> $resourceMap = new HashMap<>();
        for (StaticResource resource: $resourceList) {
            String url = resource.getUrl();
            if ($resourceMap.containsKey(url)) {
                continue;
            }
            $resourceMap.put(url, resource);
        }
        return $resourceMap;
    }

    public static List<StaticResource> getDiskResources(String prefixUrl, String dirPath) {
        List<StaticResource> resourceList = getDiskResources(dirPath);
        String _prefixUrl = getPrefixUrl(prefixUrl);
        resourceList.forEach(resource -> {
            resource.setUrl(_prefixUrl + resource.getUrl());
        });
        return resourceList;
    }

    public static List<StaticResource> getDiskResources(String dirPath) {
        List<StaticResource> resourceList = new ArrayList<>();
        try {
            String filePrefix = new File(dirPath).getAbsolutePath().replace("\\", "/");
            Map<String, DiskFileResource> resourceMap =IOUtils.loadResourceFromDiskDir(dirPath);
            resourceMap.forEach((diskPath, resourceObj) -> {
                String uri = resourceObj.getDiskFilePath().replace("\\", "/");
                String url = uri.substring(filePrefix.length());
                resourceList.add(new StaticResource(StaticResourceType.DISK_FILE, uri, url, resourceObj.getBytes()));
            });
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("get resources from disk directory location: [{}] failed.", dirPath, throwable);
            }
        }
        return resourceList;
    }

    public static List<StaticResource> getClasspathResources(String prefixUrl, String cpDirLocation) {
        List<StaticResource> resourceList = getClasspathResources(cpDirLocation);
        String _prefixUrl = getPrefixUrl(prefixUrl);
        resourceList.forEach(resource -> {
            resource.setUrl(_prefixUrl + resource.getUrl());
        });
        return resourceList;
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
            if (logger.isDebugEnabled()) {
                logger.debug("get static resources from classpath location: {}", _location);
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
                resourceList.add(new StaticResource(StaticResourceType.CLASSPATH_FILE, uri, url, contentBytes));
            }
        } catch (Throwable throwable) {
            if (logger.isErrorEnabled()) {
                logger.error("get resources from classpath directory location: [{}] failed.", cpDirLocation, throwable);
            }
        }
        return resourceList;
    }

    private static String getPrefixUrl(String prefixUrl) {
        String _prefixUrl = String.valueOf(prefixUrl).replace("\\", "/");
        while (_prefixUrl.endsWith("/") && _prefixUrl.length() > 1) {
            _prefixUrl = _prefixUrl.substring(0, _prefixUrl.length() - 1);
        }
        if (!_prefixUrl.startsWith("/")) {
            _prefixUrl = "/" + _prefixUrl;
        }
        if (Constants.ROOT_PATH.equals(_prefixUrl)) {
            _prefixUrl = StringUtils.EMPTY;
        }
        return _prefixUrl;
    }

}

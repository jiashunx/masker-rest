package io.github.jiashunx.masker.rest.framework.servlet;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.model.StaticResource;
import io.github.jiashunx.masker.rest.framework.type.StaticResourceType;
import io.github.jiashunx.masker.rest.framework.util.IOUtils;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.github.jiashunx.masker.rest.framework.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author jiashunx
 */
public class StaticResourceFinder {

    private static final Logger logger = LoggerFactory.getLogger(StaticResourceFinder.class);

    /**
     * 未定位到的静态资源(非静态对象).
     */
    private final StaticResource NotFoundStaticResource = StaticResource.buildEmpty();

    /**
     * 静态资源缓存Map(非静态对象)
     */
    private final Map<String, StaticResource> StaticResourceMap = new ConcurrentHashMap<>();
    /**
     * 静态资源缓存Map(读写锁)
     */
    private final ReentrantReadWriteLock StaticResourceMapReadWriteLock = new ReentrantReadWriteLock();

    private final MRestContext restContext;

    public StaticResourceFinder(MRestContext restContext) {
        this.restContext = Objects.requireNonNull(restContext);
    }

    public void clear() {
        StaticResourceMapReadWriteLock.writeLock().lock();
        try {
            StaticResourceMap.clear();
        } finally {
            StaticResourceMapReadWriteLock.writeLock().unlock();
        }
    }

    public StaticResource loadResource(String requestUrl0) {
        String requestUrl = MRestUtils.formatPath(requestUrl0);
        // 若不支持静态资源缓存，则每次请求均执行静态资源查找处理
        if (!this.restContext.isStaticResourcesCacheEnabled()) {
            StaticResource staticResource = loadResourceFromClasspath(requestUrl);
            if (staticResource == null) {
                staticResource = loadResourceFromDiskpath(requestUrl);
            }
            return staticResource;
        }
        StaticResource staticResource = StaticResourceMap.get(requestUrl);
        if (staticResource == null) {
            StaticResourceMapReadWriteLock.writeLock().lock();
            try {
                staticResource = StaticResourceMap.get(requestUrl);
                if (staticResource == null) {
                    staticResource = loadResourceFromClasspath(requestUrl);
                    if (staticResource == null) {
                        staticResource = loadResourceFromDiskpath(requestUrl);
                    }
                    if (staticResource == null) {
                        staticResource = NotFoundStaticResource;
                    }
                    StaticResourceMap.put(requestUrl, staticResource);
                }
            } finally {
                StaticResourceMapReadWriteLock.writeLock().unlock();
            }
        }
        if (staticResource == NotFoundStaticResource) {
            return null;
        }
        return staticResource;
    }

    private StaticResource loadResourceFromClasspath(String requestUrl0) {
        String requestUrl = MRestUtils.formatPath(requestUrl0);
        List<String> classpathResourcePrefixUrls = findPrefixUrls(requestUrl, restContext.getClasspathResourcePrefixUrls());
        for (String prefixUrl: classpathResourcePrefixUrls) {
            // requestUrl: /webjars/webjar-jquery/3.5.1/dist/jquery.min.js
            // prefixUrl: /webjars -> META-INFO/resources/webjars/
            // filePathSuffix: /webjar-jquery/3.5.1/dist/jquery.min.js
            // classpathResourcePath = META-INFO/resources/webjars/webjar-jquery/3.5.1/dist/jquery.min.js
            String filePathSuffix = UrlUtils.removePrefixSep0(requestUrl.substring(prefixUrl.length()));
            List<String> classpathResourcePaths = restContext.getClasspathResourcePaths(prefixUrl);
            for (String classpathResourcePath0: classpathResourcePaths) {
                String classpathResourcePath = UrlUtils.appendSuffixSep(classpathResourcePath0) + filePathSuffix;
                try {
                    byte[] contentBytes = IOUtils.loadBytesFromClasspath(classpathResourcePath, IOUtils.class.getClassLoader(), false);
                    if (contentBytes != null) {
                        // 若请求资源路径无后缀且文件大小小于等于256KB，则继续查找子文件，若找到子文件，则判定当前请求资源路径为文件夹
                        if (classpathResourcePath.lastIndexOf(".") < 0 && contentBytes.length <= 256 * 1024) {
                            String subFilePath = classpathResourcePath;
                            if (!subFilePath.endsWith("/")) {
                                subFilePath += "/";
                            }
                            subFilePath += new String(contentBytes, StandardCharsets.UTF_8).split("\n")[0];
                            byte[] subFileBytes = IOUtils.loadBytesFromClasspath(subFilePath, IOUtils.class.getClassLoader(), false);
                            if (subFileBytes != null) {
                                logger.warn("{} locate classpath resource: [{}] -> [{}], not found (it's a directory)", restContext.getContextDesc(), requestUrl, classpathResourcePath);
                                return null;
                            }
                        }
                        logger.info("{} locate classpath resource: [{}] -> [{}]", restContext.getContextDesc(), requestUrl, classpathResourcePath);
                        return new StaticResource(StaticResourceType.CLASSPATH_FILE, classpathResourcePath, requestUrl, contentBytes);
                    }
                } catch (Throwable throwable) {
                    logger.error("{} locate classpath resource: [{}] -> [{}], error occured", restContext.getContextDesc(), requestUrl, classpathResourcePath);
                }
            }
        }
        return null;
    }

    private StaticResource loadResourceFromDiskpath(String requestUrl0) {
        String requestUrl = MRestUtils.formatPath(requestUrl0);
        List<String> diskpathResourcePrefixUrls = findPrefixUrls(requestUrl, restContext.getDiskpathResourcePrefixUrls());
        for (String prefixUrl: diskpathResourcePrefixUrls) {
            // requestUrl: /webjars/webjar-jquery/3.5.1/dist/jquery.min.js
            // prefixUrl: /webjars -> /app/xxx/dist/webjars/
            // filePathSuffix: /webjar-jquery/3.5.1/dist/jquery.min.js
            // classpathResourcePath = /app/xxx/dist/webjars/webjar-jquery/3.5.1/dist/jquery.min.js
            String filePathSuffix = UrlUtils.removePrefixSep0(requestUrl.substring(prefixUrl.length()));
            List<String> diskpathResourcePaths = restContext.getDiskpathResourcePaths(prefixUrl);
            for (String diskpathResourcePath0: diskpathResourcePaths) {
                String diskpathResourcePath = UrlUtils.appendSuffixSep(UrlUtils.replaceWinSep(diskpathResourcePath0)) + filePathSuffix;
                try {
                    // 磁盘文件路径合法性检查
                    String path0 = UrlUtils.replaceWinSep(new File(diskpathResourcePath0).getAbsolutePath());
                    String path1 = UrlUtils.replaceWinSep(new File(diskpathResourcePath).getAbsolutePath());
                    // 文件路径不可为文件目录父级目录.
                    if (path1.startsWith(path0) && path1.length() > path0.length()) {
                        byte[] contentBytes = IOUtils.loadBytesFromDisk(diskpathResourcePath, false);
                        if (contentBytes != null) {
                            logger.info("{} locate diskpath resource: [{}] -> [{}]", restContext.getContextDesc(), requestUrl, diskpathResourcePath);
                            return new StaticResource(StaticResourceType.DISK_FILE, diskpathResourcePath, requestUrl, contentBytes);
                        }
                    }
                } catch (Throwable throwable) {
                    logger.error("{} locate diskpath resource: [{}] -> [{}], error occured", restContext.getContextDesc(), requestUrl, diskpathResourcePath);
                }
            }
        }
        return null;
    }

    private List<String> findPrefixUrls(String requestUrl0, List<String> prefixUrls) {
        List<String> retList = new ArrayList<>();
        String requestUrl = MRestUtils.formatPath(requestUrl0);
        // prefixUrl -> not null or empty("")
        for (String prefixUrl: prefixUrls) {
            if (requestUrl.startsWith(prefixUrl) && !requestUrl.equals(prefixUrl)) {
                retList.add(prefixUrl);
            }
        }
        return retList;
    }

}

package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerInitializeException;
import io.github.jiashunx.masker.rest.framework.filter.MRestDispatchFilter;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilter;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.filter.StaticResourceFilter;
import io.github.jiashunx.masker.rest.framework.handler.*;
import io.github.jiashunx.masker.rest.framework.model.ExceptionCallbackVo;
import io.github.jiashunx.masker.rest.framework.model.MRestHandlerConfig;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.github.jiashunx.masker.rest.framework.util.StringUtils;
import io.netty.handler.codec.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.*;

/**
 * @author jiashunx
 */
public class MRestContext {

    private static final Logger logger = LoggerFactory.getLogger(MRestContext.class);

    private final MRestServer restServer;
    private final String contextPath;

    public MRestContext(MRestServer restServer, String contextPath) {
        this.restServer = Objects.requireNonNull(restServer);
        this.contextPath = formatContextPath(contextPath);
    }

    public static String formatContextPath(String contextPath) {
        if (StringUtils.isBlank(contextPath)) {
            throw new IllegalArgumentException("contextPath can't be empty");
        }
        String _ctxPath = contextPath.trim();
        while (_ctxPath.endsWith(Constants.URL_PATH_SEP)) {
            if (_ctxPath.length() == 1) {
                break;
            }
            _ctxPath = _ctxPath.substring(0, _ctxPath.length() - 1);
        }
        if (!_ctxPath.startsWith(Constants.URL_PATH_SEP)) {
            _ctxPath = Constants.URL_PATH_SEP;
        }
        return _ctxPath;
    }

    public MRestServer getRestServer() {
        return restServer;
    }
    public String getContextPath() {
        return contextPath;
    }


    public void init() {
        // mapping处理
        for (Runnable mappingTask: mappingTaskList) {
            mappingTask.run();
        }
        // filter处理
        for (Runnable filterTask: filterTaskList) {
            filterTask.run();
        }
        // 静态资源处理
        List<String> classpathResources = getClasspathResources();
        if (logger.isInfoEnabled()) {
            logger.info("Context[{}] reload classpath resources: {}", getContextPath(), classpathResources);
        }
        ((StaticResourceFilter) staticResourceFilter).reloadClasspathResource(classpathResources);
        List<String> diskResources = getDiskResources();
        if (logger.isInfoEnabled()) {
            logger.info("Context[{}] reload disk resources: {}", getContextPath(), diskResources);
        }
        ((StaticResourceFilter) staticResourceFilter).reloadDiskResource(diskResources);
    }


    /**************************************************** SEP ****************************************************/
    /**************************************************** SEP ****************************************************/

    private final Map<String, Map<HttpMethod, MRestHandler>> urlMappingHandler = new HashMap<>();

    /**
     * 添加url映射处理的任务(在服务启动时统一添加).
     */
    private final List<Runnable> mappingTaskList = new ArrayList<>();

    /**
     * 指定url是否是已指定映射处理.
     * @param requestURL requestURL
     * @param methods methods
     * @return boolean
     */
    public boolean isMappingURL(String requestURL, HttpMethod... methods) {
        if (urlMappingHandler.containsKey(requestURL)) {
            Map<HttpMethod, MRestHandler> handlerMap = urlMappingHandler.get(requestURL);
            for (HttpMethod method: methods) {
                if (handlerMap.get(method) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 检查映射url正确性.
     * @param url url
     * @param methods methods
     */
    private void checkMappingUrl(String url, HttpMethod... methods) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException(String.format("Context[%s] illegal mapping url: %s", getContextPath(), url));
        }
        if (isMappingURL(url, methods)) {
            throw new MRestServerInitializeException(String.format("Context[%s] url mapping conflict: %s", getContextPath(), url));
        }
    }

    private void checkMappingHandler(MRestHandler handler) {
        if (handler == null || handler.getHandler() == null) {
            throw new NullPointerException(String.format("Context[%s] mapping handler cann't be null", getContextPath()));
        }
    }

    public MRestContext mapping(String url, Supplier<?> handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public MRestContext mapping(String url, Supplier<?> handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public MRestContext mapping(String url, Supplier<?> handler, MRestHandlerConfig config, HttpMethod... methods) {
        return mapping(url, new MRestHandlerSupplier<>(url, handler, config, methods), methods);
    }

    public MRestContext mapping(String url, Runnable handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public MRestContext mapping(String url, Runnable handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public MRestContext mapping(String url, Runnable handler, MRestHandlerConfig config, HttpMethod... methods) {
        return mapping(url, new MRestHandlerConsumerVoid(url, handler, config, methods), methods);
    }

    public MRestContext mapping(String url, Consumer<MRestRequest> handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public MRestContext mapping(String url, Consumer<MRestRequest> handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public MRestContext mapping(String url, Consumer<MRestRequest> handler, MRestHandlerConfig config, HttpMethod... methods) {
        return mapping(url, new MRestHandlerConsumerReq<>(url, handler, config, methods), methods);
    }

    public MRestContext mapping(String url, BiConsumer<MRestRequest, MRestResponse> handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public MRestContext mapping(String url, BiConsumer<MRestRequest, MRestResponse> handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public MRestContext mapping(String url, BiConsumer<MRestRequest, MRestResponse> handler, MRestHandlerConfig config, HttpMethod... methods) {
        return mapping(url, new MRestHandlerConsumerReqResp<>(url, handler, config, methods), methods);
    }

    public <R> MRestContext mapping(String url, Function<MRestRequest, R> handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public <R> MRestContext mapping(String url, Function<MRestRequest, R> handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public <R> MRestContext mapping(String url, Function<MRestRequest, R> handler, MRestHandlerConfig config, HttpMethod... methods) {
        return mapping(url, new MRestHandlerFunction<>(url, handler, config, methods), methods);
    }

    public <R> MRestContext mapping(String url, BiFunction<MRestRequest, MRestResponse, R> handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public <R> MRestContext mapping(String url, BiFunction<MRestRequest, MRestResponse, R> handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public <R> MRestContext mapping(String url, BiFunction<MRestRequest, MRestResponse, R> handler, MRestHandlerConfig config, HttpMethod... methods) {
        return mapping(url, new MRestHandlerBiFunction<>(url, handler, config, methods), methods);
    }

    private synchronized MRestContext mapping(String url, MRestHandler handler, HttpMethod... methods) {
        getRestServer().checkServerState();
        mappingTaskList.add(() -> {
            checkMappingUrl(url, methods);
            checkMappingHandler(handler);
            Map<HttpMethod, MRestHandler> handlerMap = urlMappingHandler.computeIfAbsent(url, k -> new HashMap<>());
            for (HttpMethod method: methods) {
                handlerMap.put(method, handler);
            }
            if (logger.isInfoEnabled()) {
                logger.info("Context[{}] register url handler success, {}, {}", getContextPath(), methods, url);
            }
        });
        return this;
    }

    public MRestHandler getUrlMappingHandler(String requestURL, HttpMethod method) {
        Map<HttpMethod, MRestHandler> handlerMap = urlMappingHandler.get(requestURL);
        if (handlerMap != null) {
            return handlerMap.get(method);
        }
        return null;
    }

    public <R> MRestContext get(String url, Supplier<R> handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public <R> MRestContext get(String url, Supplier<R> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public <R> MRestContext get(String url, Supplier<R> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.GET);
    }

    public MRestContext get(String url, Runnable handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public MRestContext get(String url, Runnable handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public MRestContext get(String url, Runnable handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.GET);
    }

    public MRestContext get(String url, Consumer<MRestRequest> handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public MRestContext get(String url, Consumer<MRestRequest> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public MRestContext get(String url, Consumer<MRestRequest> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.GET);
    }

    public <R> MRestContext get(String url, Function<MRestRequest, R> handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public <R> MRestContext get(String url, Function<MRestRequest, R> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public <R> MRestContext get(String url, Function<MRestRequest, R> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.GET);
    }

    public <R> MRestContext get(String url, BiFunction<MRestRequest, MRestResponse, R> handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public <R> MRestContext get(String url, BiFunction<MRestRequest, MRestResponse, R> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public <R> MRestContext get(String url, BiFunction<MRestRequest, MRestResponse, R> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.GET);
    }

    public MRestContext get(String url, BiConsumer<MRestRequest, MRestResponse> handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public MRestContext get(String url, BiConsumer<MRestRequest, MRestResponse> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public MRestContext get(String url, BiConsumer<MRestRequest, MRestResponse> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.GET);
    }

    public MRestContext post(String url, Consumer<MRestRequest> handler) {
        return mapping(url, handler, HttpMethod.POST);
    }

    public MRestContext post(String url, Consumer<MRestRequest> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.POST);
    }

    public MRestContext post(String url, Consumer<MRestRequest> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.POST);
    }

    public <R> MRestContext post(String url, Function<MRestRequest, R> handler) {
        return mapping(url, handler, HttpMethod.POST);
    }

    public <R> MRestContext post(String url, Function<MRestRequest, R> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.POST);
    }

    public <R> MRestContext post(String url, Function<MRestRequest, R> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.POST);
    }

    public <R> MRestContext post(String url, BiFunction<MRestRequest, MRestResponse, R> handler) {
        return mapping(url, handler, HttpMethod.POST);
    }

    public <R> MRestContext post(String url, BiFunction<MRestRequest, MRestResponse, R> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.POST);
    }

    public <R> MRestContext post(String url, BiFunction<MRestRequest, MRestResponse, R> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.POST);
    }

    public MRestContext post(String url, BiConsumer<MRestRequest, MRestResponse> handler) {
        return mapping(url, handler, HttpMethod.POST);
    }

    public MRestContext post(String url, BiConsumer<MRestRequest, MRestResponse> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.POST);
    }

    public MRestContext post(String url, BiConsumer<MRestRequest, MRestResponse> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.POST);
    }

    public MRestContext fileupload(String url, Consumer<MRestRequest> handler) {
        return post(url, handler);
    }

    public <R> MRestContext fileupload(String url, Function<MRestRequest, R> handler) {
        return post(url, handler);
    }

    public <R> MRestContext fileupload(String url, BiFunction<MRestRequest, MRestResponse, R> handler) {
        return post(url, handler);
    }

    public MRestContext fileupload(String url, BiConsumer<MRestRequest, MRestResponse> handler) {
        return post(url, handler);
    }

    public MRestContext filedownload(String url, BiConsumer<MRestRequest, MRestResponse> handler) {
        return get(url, handler);
    }


    /**************************************************** SEP ****************************************************/
    /**************************************************** SEP ****************************************************/


    /**
     * filter映射处理.
     */
    private final Map<String, List<MRestFilter>> filterMap = new HashMap<>();
    /**
     * 请求分发处理.
     */
    private final MRestFilter requestFilter = new MRestDispatchFilter();
    /**
     * 配置的静态资源classpath扫描路径.
     */
    private final Set<String> classpathResources = new HashSet<>();
    /**
     * 配置的静态资源磁盘扫描路径.
     */
    private final Set<String> diskResources = new HashSet<>();
    /**
     * 静态资源处理.
     */
    private final MRestFilter staticResourceFilter = new StaticResourceFilter(this);
    /**
     * 添加filter的任务(在服务启动时统一添加).
     */
    private final List<Runnable> filterTaskList = new ArrayList<>();

    public MRestFilterChain getCommonStaticResourceFilterChain(String requestURL) {
        List<MRestFilter> filterList = new LinkedList<>();
        filterList.add(staticResourceFilter);
        filterList.add(requestFilter);
        return new MRestFilterChain(this, filterList.toArray(new MRestFilter[0]));
    }

    public MRestFilterChain getFilterChain(String requestURL) {
        Set<MRestFilter> filterSet = new HashSet<>();
        filterMap.forEach((urlPattern, filterList) -> {
            String pattern = "^" + urlPattern.replace("*", "\\S*") + "$";
            if (requestURL.matches(pattern)) {
                filterSet.addAll(filterList);
            }
        });
        // 对filter进行排序, 按照order小到大进行顺序排序.
        LinkedList<MRestFilter> filterList = new LinkedList<>(filterSet);
        filterList.sort((filter0, filter1) -> {
            int order0 = filter0.order();
            int order1 = filter1.order();
            return order0 - order1;
        });
        filterList.addLast(staticResourceFilter);
        filterList.addLast(requestFilter);
        return new MRestFilterChain(this, filterList.toArray(new MRestFilter[0]));
    }

    /**
     * 添加filter, 自动扫描filter注解, 获取urlPattern.
     * @param filterArr filterArr
     * @return MRestContext
     */
    public synchronized MRestContext filter(MRestFilter... filterArr) {
        for (MRestFilter filter: filterArr) {
            String[] urlPatterns = filter.urlPatterns();
            if (urlPatterns == null || urlPatterns.length == 0) {
                urlPatterns = Constants.DEFAULT_FILTER_URLPATTERNS;
            }
            filter0(filter, urlPatterns);
        }
        return this;
    }

    /**
     * 添加filter, 根据传入urlPattern来进行匹配.
     * @param urlPattern urlPattern
     * @param filterArr filterArr
     * @return MRestContext
     */
    public synchronized MRestContext filter(String urlPattern, MRestFilter... filterArr) {
        for (MRestFilter filter: filterArr) {
            filter0(filter, urlPattern);
        }
        return this;
    }

    /**
     * 添加filter, 一次指定多个urlPattern.
     * @param filter filter
     * @param urlPatterns urlPattern array.
     * @return MRestContext
     */
    public synchronized MRestContext filter(MRestFilter filter, String... urlPatterns) {
        return filter0(filter, urlPatterns);
    }

    private synchronized MRestContext filter0(MRestFilter filter, String... urlPatterns) {
        getRestServer().checkServerState();
        filterTaskList.add(() -> {
            MRestFilter restFilter = Objects.requireNonNull(filter);
            if (urlPatterns.length == 0) {
                throw new IllegalArgumentException(String.format("Context[%s] can't assign empty urlPatterns to filter: %s", getContextPath(), filter.filterName()));
            }
            for (String urlPattern: urlPatterns) {
                filterMap.computeIfAbsent(urlPattern, k -> new ArrayList<>()).add(restFilter);
                if (logger.isInfoEnabled()) {
                    logger.info("Context[{}] register filter success, {} -> {}", getContextPath(), urlPattern, filter.filterName());
                }
            }
        });
        return this;
    }

    public MRestContext addDefaultClasspathResource() {
        return addClasspathResources(
                MRestUtils.getDefaultServerConfig().getClasspathResources().toArray(new String[0]));
    }

    public MRestContext addClasspathResource(String path) {
        return addClasspathResources(new String[] { path });
    }

    public synchronized MRestContext addClasspathResources(String[] pathArr) {
        if (pathArr != null) {
            for (String path: pathArr) {
                classpathResources.add(formatClasspathResourcePath(path));
            }
        }
        return this;
    }

    private String formatClasspathResourcePath(String path) {
        String location = String.valueOf(path).trim();
        if (!location.endsWith(Constants.URL_PATH_SEP)) {
            location = location + Constants.URL_PATH_SEP;
        }
        while (location.startsWith(Constants.URL_PATH_SEP)) {
            if (location.length() == 1) {
                break;
            }
            location = location.substring(1);
        }
        return location;
    }

    public List<String> getClasspathResources() {
        return new ArrayList<>(classpathResources);
    }

    public MRestContext addDiskResource(String path) {
        return addDiskResources(new String[] { path });
    }

    public MRestContext addDiskResources(String[] pathArr) {
        if (pathArr != null) {
            for (String path: pathArr) {
                diskResources.add(formatDiskResourcePath(path));
            }
        }
        return this;
    }

    private String formatDiskResourcePath(String path) {
        return path;
    }

    public List<String> getDiskResources() {
        return new ArrayList<>(diskResources);
    }


    /**************************************************** SEP ****************************************************/
    /**************************************************** SEP ****************************************************/


    private Consumer<ExceptionCallbackVo> defaultErrorHandler = null;

    public Consumer<ExceptionCallbackVo> getDefaultErrorHandler() {
        return defaultErrorHandler;
    }

    public MRestContext defaultErrorHandler(Consumer<ExceptionCallbackVo> defaultErrorHandler) {
        this.defaultErrorHandler = Objects.requireNonNull(defaultErrorHandler);
        return this;
    }

}

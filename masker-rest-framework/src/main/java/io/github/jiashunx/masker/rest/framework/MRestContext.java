package io.github.jiashunx.masker.rest.framework;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestMappingException;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerInitializeException;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.global.SharedObjects;
import io.github.jiashunx.masker.rest.framework.model.*;
import io.github.jiashunx.masker.rest.framework.servlet.*;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilter;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChainOfDefault;
import io.github.jiashunx.masker.rest.framework.function.VoidFunc;
import io.github.jiashunx.masker.rest.framework.handler.*;
import io.github.jiashunx.masker.rest.framework.util.*;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.Collectors;

/**
 * @author jiashunx
 */
public class MRestContext {

    private static final Logger logger = LoggerFactory.getLogger(MRestContext.class);

    private final MRestServer restServer;
    private final String contextPath;
    private final StaticResourceFinder staticResourceFinder;
    private final Map<String, MWebsocketContext> websocketContextMap = new ConcurrentHashMap<>();

    public MRestContext(MRestServer restServer, String contextPath) {
        this.restServer = Objects.requireNonNull(restServer);
        this.contextPath = MRestUtils.formatContextPath(contextPath);
        this.staticResourceFinder = new StaticResourceFinder(this);
    }

    public MRestServer getRestServer() {
        return restServer;
    }
    public String getContextPath() {
        return contextPath;
    }

    public String getContextDesc() {
        return String.format("%s Context[%s]", getRestServer().getServerDesc(), getContextPath());
    }

    public StaticResourceFinder getStaticResourceFinder() {
        return this.staticResourceFinder;
    }

    void initResources() {
        // 添加框架提供的静态资源
        addClasspathResources("/masker-rest", new String[]{ "masker-rest/static/" });
        // 添加webjars的支持(其实也无需显式支持, webjars在META-INF/resources目录下)
        addClasspathResource("/webjars", "META-INF/resources/webjars/");
    }

    void init() {
        // 添加默认context-path
        if (getWebsocketContext(Constants.DEFAULT_WEBSOCKET_CONTEXT_PATH) == null) {
            websocketContextMap.put(Constants.DEFAULT_WEBSOCKET_CONTEXT_PATH, new MWebsocketContext(this.restServer, this, Constants.DEFAULT_WEBSOCKET_CONTEXT_PATH));
        }

        // websocket-context初始化
        websocketContextMap.forEach((key, websocketContext) -> {
            websocketContext.init();
        });

        // mapping处理
        for (VoidFunc mappingTask: mappingTaskList) {
            mappingTask.doSomething();
        }
        // servlet处理
        for (VoidFunc servletTask: servletTaskList) {
            servletTask.doSomething();
        }
        // filter处理
        for (VoidFunc filterTask: filterTaskList) {
            filterTask.doSomething();
        }
        // 静态资源处理
        if (isStaticResourcesCacheEnabled()) {
            reloadResource();
            if (isAutoRefreshStaticResources()) {
                new Thread(() -> {
                    while (true) {
                        try {
                            Thread.sleep(getAutoRefreshStaticResourcesPeriod());
                            reloadResource();
                        } catch (Throwable throwable) {
                            logger.error("{} reload static resource failed", getContextDesc(), throwable);
                        }
                    }
                }, "ResourceReload" + restServer.getListenPort() + "_" + getContextPath()).start();
            }
        }
    }

    private void reloadResource() {
        staticResourceFinder.clear();
    }

    /**************************************************** SEP ****************************************************/
    /**************************************************** SEP ****************************************************/

    private final Map<String, Map<HttpMethod, MRestHandler>> urlMappingHandler = new HashMap<>();

    /**
     * 添加url映射处理的任务(在服务启动时统一添加).
     */
    private final List<VoidFunc> mappingTaskList = new ArrayList<>();

    /**
     * 指定url是否是已指定映射处理.
     * @param requestUrl requestUrl
     * @param methods methods
     * @return boolean
     */
    public boolean isMappingURL(String requestUrl, HttpMethod... methods) {
        if (urlMappingHandler.containsKey(requestUrl)) {
            Map<HttpMethod, MRestHandler> handlerMap = urlMappingHandler.get(requestUrl);
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
        if (StringUtils.isEmpty(url)) {
            throw new IllegalArgumentException(String.format("%s illegal mapping url: %s", getContextDesc(), url));
        }
        if (isMappingURL(url, methods)) {
            throw new MRestServerInitializeException(String.format("%s url mapping conflict: %s", getContextDesc(), url));
        }
    }

    private void checkMappingHandler(MRestHandler handler) {
        if (handler == null || handler.getHandler() == null) {
            throw new NullPointerException(String.format("%s mapping handler cann't be null", getContextDesc()));
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

    public MRestContext mapping(String url, VoidFunc handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public MRestContext mapping(String url, VoidFunc handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public MRestContext mapping(String url, VoidFunc handler, MRestHandlerConfig config, HttpMethod... methods) {
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
            logger.info("{} register url handler: {} -> {}", getContextDesc(), methods, url);
        });
        return this;
    }

    public MRestHandler getUrlMappingHandler(String requestUrl, HttpMethod method) {
        MRestHandler handler = null;
        Map<HttpMethod, MRestHandler> handlerMap = urlMappingHandler.get(requestUrl);
        if (handlerMap != null) {
            handler = handlerMap.get(method);
        }
        if (logger.isDebugEnabled()) {
            if (handler != null) {
                logger.debug("{} found url handler: [{}] -> [{}]", getContextDesc(), requestUrl, handler.getUrl());
            } else {
                logger.debug("{} no url handler found for url: [{}]", getContextDesc(), requestUrl);
            }
        }
        return handler;
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

    public MRestContext get(String url, VoidFunc handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public MRestContext get(String url, VoidFunc handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public MRestContext get(String url, VoidFunc handler, MRestHandlerConfig config) {
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
     * servlet映射处理.
     */
    private final Map<String, UrlMappingServlet> servletMap = new LinkedHashMap<>();
    /**
     * filter映射处理.
     */
    private final Map<String, List<MRestFilter>> filterMap = new HashMap<>();
    /**
     * 请求分发处理.
     */
    private final MRestServlet dispatchServlet = new MRestDispatchServlet();
    /**
     * 静态资源匹配处理.
     */
    private final MRestServlet staticResourceServlet = new StaticResourceServlet();
    /**
     * 请求处理末尾Servlet(扫尾工作).
     */
    private final MRestServlet lastServlet = new LastServlet();
    /**
     * 配置的静态资源classpath扫描路径, 按照配置的先后顺序进行扫描, classpath静态资源扫描顺序高于磁盘静态资源.
     */
    private final Map<String, List<String>> classpathResources = new HashMap<>();
    /**
     * 配置的静态资源磁盘扫描路径, 按照配置的先后顺序进行扫描.
     */
    private final Map<String, List<String>> diskpathResources = new HashMap<>();
    /**
     * 添加servlet的任务(在服务启动时统一添加).
     */
    private final List<VoidFunc> servletTaskList = new ArrayList<>();
    /**
     * 添加filter的任务(在服务启动时统一添加).
     */
    private final List<VoidFunc> filterTaskList = new ArrayList<>();

    public MRestServlet getServlet(String requestUrl) {
        List<UrlMappingServlet> mappingServletList = new ArrayList<>();
        // 扩展名匹配
        AtomicReference<UrlMappingServlet> extRef = new AtomicReference<>();
        // 路径匹配
        AtomicReference<UrlMappingServlet> pathMatchRef = new AtomicReference<>();
        // 精确匹配 - 带占位符
        AtomicReference<UrlMappingServlet> strictlyRef0 = new AtomicReference<>();
        // 精确匹配 - 不带占位符
        AtomicReference<UrlMappingServlet> strictlyRef1 = new AtomicReference<>();
        servletMap.forEach((_up, urlMappingServlet) -> {
            UrlMatchModel urlMatchModel = new UrlMatchModel(requestUrl, _up);
            if (urlMatchModel.isMatched()) {
                if (urlMatchModel.isPatternExt()) {
                    if (extRef.get() != null) {
                        throw new MRestMappingException(
                                String.format("%s found more than one servlet mapping handler for url: %s, urlPattern: %s|%s"
                                        , getContextDesc(), requestUrl, extRef.get().getUrlPatternModel().getUrlPattern(), urlMatchModel.getUrlPattern()));
                    }
                    extRef.set(urlMappingServlet);
                }
                if (urlMatchModel.isPatternPathMatch()) {
                    if (pathMatchRef.get() != null) {
                        throw new MRestMappingException(
                                String.format("%s found more than one servlet mapping handler for url: %s, urlPattern: %s|%s"
                                        , getContextDesc(), requestUrl, pathMatchRef.get().getUrlPatternModel().getUrlPattern(), urlMatchModel.getUrlPattern()));
                    }
                    pathMatchRef.set(urlMappingServlet);
                }
                if (urlMatchModel.isPatternStrictly()) {
                    if (urlMatchModel.getUrlPatternModel().isSupportPlaceholder()) {
                        if (strictlyRef0.get() != null) {
                            throw new MRestMappingException(
                                    String.format("%s found more than one servlet mapping handler for url: %s, urlPattern: %s|%s"
                                            , getContextDesc(), requestUrl, strictlyRef0.get().getUrlPatternModel().getUrlPattern(), urlMatchModel.getUrlPattern()));
                        }
                        SharedObjects.getServerThreadModel().getRestRequest().addPlaceholderKv(urlMatchModel.getPlaceholderMap());
                        strictlyRef0.set(urlMappingServlet);
                    } else {
                        if (strictlyRef1.get() != null) {
                            throw new MRestMappingException(
                                    String.format("%s found more than one servlet mapping handler for url: %s, urlPattern: %s|%s"
                                            , getContextDesc(), requestUrl, strictlyRef1.get().getUrlPatternModel().getUrlPattern(), urlMatchModel.getUrlPattern()));
                        }
                        strictlyRef1.set(urlMappingServlet);
                    }
                }
            }
        });
        if (strictlyRef1.get() != null) {
            mappingServletList.add(strictlyRef1.get());
        }
        if (strictlyRef0.get() != null && pathMatchRef.get() != null) {
            UrlMappingServlet servlet0 = strictlyRef0.get();
            UrlMappingServlet servlet1 = pathMatchRef.get();
            // 路径匹配度
            int count0 = servlet0.getUrlPatternModel().getActualPathMatchCount(requestUrl);
            int count1 = servlet1.getUrlPatternModel().getActualPathMatchCount(requestUrl);
            if (count0 == count1) {
                throw new MRestMappingException(
                        String.format("%s found more than one servlet mapping handler for url: %s, urlPattern: %s|%s"
                                , getContextDesc(), requestUrl, servlet0.getUrlPatternModel().getUrlPattern(), servlet1.getUrlPatternModel().getUrlPattern()));
            }
            // 相同匹配模式 ((/*)|(\\S+)*)* 优先选择路径匹配度高的匹配模式
            mappingServletList.add(count0 > count1 ? servlet0 : servlet1);
        } else {
            if (strictlyRef0.get() != null) {
                mappingServletList.add(strictlyRef0.get());
            }
            if (pathMatchRef.get() != null) {
                mappingServletList.add(pathMatchRef.get());
            }
        }
        if (extRef.get() != null) {
            mappingServletList.add(extRef.get());
        }
        if (mappingServletList.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("{} no servlet found for url: [{}]", getContextDesc(), requestUrl);
            }
            return null;
        }
        MRestServlet servlet = mappingServletList.get(0).getRestServlet();
        if (logger.isDebugEnabled()) {
            logger.debug("{} found url servlet: [{}] -> [{}]", getContextDesc(), requestUrl, servlet.servletName());
        }
        return servlet;
    }

    public synchronized MRestContext servlet(MRestServlet... servletArr) {
        for (MRestServlet servlet: servletArr) {
            servlet(servlet);
        }
        return this;
    }

    public synchronized MRestContext servlet(MRestServlet servlet) {
        if (servlet instanceof AbstractRestServlet) {
            AbstractRestServlet restServlet = (AbstractRestServlet) servlet;
            List<String> urlList = restServlet.getMappingUrlList();
            urlList.forEach(url -> {
                servlet(url, servlet);
            });
            return this;
        }
        return servlet(servlet.urlPattern(), servlet);
    }

    /**
     * 三种匹配规则：
     * 1、精确匹配：确定url进行匹配，例：/user/id
     * 2、路径匹配：以"/"开头并以"/*"结尾，例：/*，/user/*
     * 3、扩展名匹配，以"*."开头的字符串用于拓展名匹配，例：*.do
     * @param urlPattern 匹配规则
     * @param servlet servlet实例
     * @return MRestContext
     */
    public synchronized MRestContext servlet(String urlPattern, MRestServlet servlet) {
        getRestServer().checkServerState();
        servletTaskList.add(() -> {
            UrlPatternModel urlPatternModel = new UrlPatternModel(urlPattern);
            String $urlPattern = urlPatternModel.getUrlPattern();
            List<UrlPatternModel> urlPatternModelList = servletMap.values().stream().map(UrlMappingServlet::getUrlPatternModel).collect(Collectors.toList());
            for (UrlPatternModel patternModel: urlPatternModelList) {
                if (patternModel.equals(urlPatternModel)) {
                    throw new MRestMappingException(String.format("%s mapping servlet conflict, urlPattern: %s | %s", getContextDesc(), $urlPattern, patternModel.getUrlPattern()));
                }
            }
            MRestServlet restServlet = Objects.requireNonNull(servlet);
            servletMap.put($urlPattern, new UrlMappingServlet(urlPatternModel, restServlet));
            logger.info("{} register servlet: {} -> {}", getContextDesc(), $urlPattern, restServlet.servletName());
        });
        return this;
    }

    public MRestFilterChain getFilterChain(String requestUrl) {
        Set<MRestFilter> filterSet = new HashSet<>();
        filterMap.forEach((urlPattern, filterList) -> {
            String pattern = "^" + urlPattern.replace("*", "(\\S|\\s)*") + "$";
            if (requestUrl.matches(pattern)) {
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
        MRestServlet servlet = getServlet(requestUrl);
        // servlet包装为filter执行
        if (servlet != null) {
            filterList.addLast(new MRestServletAdapter() {
                @Override
                public void doFilter(MRestRequest request, MRestResponse response, MRestFilterChain filterChain) {
                    servlet.service(request, response);
                    // servlet执行完成，不在filterChain中向后路由
                    // write方法未执行过, 直接返回成功状态码
                    if (!response.isWriteMethodInvoked()) {
                        response.write(HttpResponseStatus.OK);
                    }
                    filterChain.doFilter(request, response);
                }
            });
        }
        // rest请求分发处理.
        filterList.addLast((request, response, filterChain) -> {
            dispatchServlet.service(request, response);
            filterChain.doFilter(request, response);
        });
        filterList.addLast((request, response, filterChain) -> {
            staticResourceServlet.service(request, response);
            filterChain.doFilter(request, response);
        });
        filterList.addLast((request, response, filterChain) -> {
            lastServlet.service(request, response);
            filterChain.doFilter(request, response);
        });
        return new MRestFilterChainOfDefault(this, filterList.toArray(new MRestFilter[0]));
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
                throw new IllegalArgumentException(String.format("%s can't assign empty urlPatterns to filter: %s", getContextDesc(), filter.filterName()));
            }
            for (String urlPattern: urlPatterns) {
                filterMap.computeIfAbsent(urlPattern, k -> new ArrayList<>()).add(restFilter);
                logger.info("{} register filter: {} -> {}", getContextDesc(), urlPattern, filter.filterName());
            }
        });
        return this;
    }

    public MRestContext addDefaultClasspathResource() {
        return addDefaultClasspathResource(Constants.ROOT_PATH);
    }

    public MRestContext addDefaultClasspathResource(String prefixUrl) {
        return addClasspathResources(prefixUrl,
                MRestUtils.getDefaultServerConfig().getClasspathResources().toArray(new String[0]));
    }

    public MRestContext addClasspathResource(String path) {
        return addClasspathResource(Constants.ROOT_PATH, path);
    }

    public MRestContext addClasspathResource(String prefixUrl, String path) {
        return addClasspathResources(prefixUrl, new String[] { path });
    }

    public MRestContext addClasspathResources(String[] pathArr) {
        return addClasspathResources(Constants.ROOT_PATH, pathArr);
    }

    public synchronized MRestContext addClasspathResources(String prefixUrl, String[] pathArr) {
        getRestServer().checkServerState();
        if (pathArr != null) {
            if (StringUtils.isEmpty(prefixUrl)) {
                throw new IllegalArgumentException("classpath resource prefixUrl can't be empty.");
            }
            for (String path0: pathArr) {
                if (StringUtils.isEmpty(path0)) {
                    throw new IllegalArgumentException("classpath resource target path can't be empty.");
                }
                String path = UrlUtils.appendSuffixSep(UrlUtils.replaceWinSep(path0));
                List<String> pathList = classpathResources.computeIfAbsent(prefixUrl, k -> new ArrayList<>());
                if (!pathList.contains(path)) {
                    pathList.add(path);
                    logger.info("{} add classpath resource: [{}] -> [{}]", getContextDesc(), prefixUrl, path);
                }
            }
        }
        return this;
    }

    public List<String> getClasspathResourcePrefixUrls() {
        return new ArrayList<>(classpathResources.keySet());
    }

    public List<String> getClasspathResourcePaths(String prefixUrl) {
        return new ArrayList<>(classpathResources.getOrDefault(prefixUrl, Collections.emptyList()));
    }

    public MRestContext addDiskpathResource(String path) {
        return addDiskpathResource(Constants.ROOT_PATH, path);
    }

    public MRestContext addDiskpathResource(String prefixUrl, String path) {
        return addDiskpathResources(prefixUrl, new String[] { path });
    }

    public MRestContext addDiskpathResources(String[] pathArr) {
        return addDiskpathResources(Constants.ROOT_PATH, pathArr);
    }

    public synchronized MRestContext addDiskpathResources(String prefixUrl, String[] pathArr) {
        getRestServer().checkServerState();
        if (pathArr != null) {
            if (StringUtils.isEmpty(prefixUrl)) {
                throw new IllegalArgumentException("diskpath resource prefixUrl can't be empty.");
            }
            for (String path0: pathArr) {
                if (StringUtils.isEmpty(path0)) {
                    throw new IllegalArgumentException("diskpath resource target path can't be empty.");
                }
                String path = UrlUtils.appendSuffixSep(UrlUtils.replaceWinSep(path0));
                List<String> pathList = diskpathResources.computeIfAbsent(prefixUrl, k -> new ArrayList<>());
                if (!pathList.contains(path)) {
                    pathList.add(path);
                    logger.info("{} add diskpath resource: [{}] -> [{}]", getContextDesc(), prefixUrl, path);
                }
            }
        }
        return this;
    }

    public List<String> getDiskpathResourcePrefixUrls() {
        return new ArrayList<>(diskpathResources.keySet());
    }

    public List<String> getDiskpathResourcePaths(String prefixUrl) {
        return new ArrayList<>(diskpathResources.getOrDefault(prefixUrl, Collections.emptyList()));
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

    private Supplier<ObjectMapper> objectMapperSupplier;

    public MRestContext setObjectMapperSupplier(Supplier<ObjectMapper> objectMapperSupplier) {
        this.objectMapperSupplier = Objects.requireNonNull(objectMapperSupplier);
        return this;
    }

    public Supplier<ObjectMapper> getObjectMapperSupplier() {
        return objectMapperSupplier;
    }

    /**
     * 是否允许静态资源缓存，默认false-不缓存（每次请求均重新查找静态资源），若为true则支持静态资源缓存
     */
    private volatile boolean staticResourcesCacheEnabled = false;
    /**
     * 静态资源缓存动态刷新，默认false-不刷新（静态资源找不到就是找不到了），若为true则支持静态资源缓存动态刷新
     */
    private volatile boolean autoRefreshStaticResources = false;
    /**
     * 静态资源缓存动态刷新默认时间间隔（秒）
     */
    private static final long DEFAULT_REFRESH_PERIOD = 60*1000L;
    /**
     * 静态资源缓存动态刷新时间间隔（秒）
     */
    private volatile long autoRefreshStaticResourcesPeriod = DEFAULT_REFRESH_PERIOD;

    public synchronized MRestContext setStaticResourcesCacheEnabled(boolean staticResourcesCacheEnabled) {
        restServer.checkServerState();
        this.staticResourcesCacheEnabled = staticResourcesCacheEnabled;
        return this;
    }

    public boolean isStaticResourcesCacheEnabled() {
        return staticResourcesCacheEnabled;
    }

    // 已更新方法签名: setAutoRefreshStaticResources(boolean)
    @Deprecated
    public synchronized MRestContext autoRefreshStaticResources(boolean autoRefreshStaticResources) {
       return setAutoRefreshStaticResources(autoRefreshStaticResources);
    }

    // 已更新方法签名: setAutoRefreshStaticResources(boolean,long)
    @Deprecated
    public synchronized MRestContext autoRefreshStaticResources(boolean autoRefreshStaticResources, long autoRefreshStaticResourcesPeriod) {
        return setAutoRefreshStaticResources(autoRefreshStaticResources, autoRefreshStaticResourcesPeriod);
    }

    public synchronized MRestContext setAutoRefreshStaticResources(boolean autoRefreshStaticResources) {
        return setAutoRefreshStaticResources(autoRefreshStaticResources, DEFAULT_REFRESH_PERIOD);
    }

    public synchronized MRestContext setAutoRefreshStaticResources(boolean autoRefreshStaticResources, long autoRefreshStaticResourcesPeriod) {
        if (autoRefreshStaticResourcesPeriod <= 0) {
            throw new IllegalArgumentException();
        }
        restServer.checkServerState();
        this.autoRefreshStaticResources = autoRefreshStaticResources;
        this.autoRefreshStaticResourcesPeriod = autoRefreshStaticResourcesPeriod;
        return this;
    }

    public boolean isAutoRefreshStaticResources() {
        return autoRefreshStaticResources;
    }

    public long getAutoRefreshStaticResourcesPeriod() {
        return autoRefreshStaticResourcesPeriod;
    }

    private volatile String indexUrl = null;

    public synchronized MRestContext setIndexUrl(String indexUrl) {
        restServer.checkServerState();
        this.indexUrl = Objects.requireNonNull(indexUrl).trim();
        return this;
    }

    public String getIndexUrl() {
        return indexUrl;
    }

    public MWebsocketContext websocketContext() {
        return websocketContext(Constants.DEFAULT_WEBSOCKET_CONTEXT_PATH);
    }

    public synchronized MWebsocketContext websocketContext(String websocketUrl) {
        MWebsocketContext websocketContext = getWebsocketContext(websocketUrl);
        if (websocketContext == null) {
            String _ctxPath = MRestUtils.formatWebsocketContextPath(websocketUrl);
            websocketContext = new MWebsocketContext(this.restServer, this, _ctxPath);
            websocketContextMap.put(_ctxPath, websocketContext);
        }
        return websocketContext;
    }

    public List<String> getWebsocketContextList() {
        return new ArrayList<>(websocketContextMap.keySet());
    }

    public MWebsocketContext getWebsocketContext(String websocketUrl) {
        return websocketContextMap.get(MRestUtils.formatWebsocketContextPath(websocketUrl));
    }

    private final Map<String, Object> globalObjects = new ConcurrentHashMap<>();

    public MRestContext setGlobalObject(String key, Object object) {
        globalObjects.put(key, object);
        return this;
    }

    public Object getGlobalObject(String key) {
        return globalObjects.get(key);
    }

}

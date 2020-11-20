package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerInitializeException;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilter;
import io.github.jiashunx.masker.rest.framework.filter.MRestFilterChain;
import io.github.jiashunx.masker.rest.framework.filter.MRestDispatchFilter;
import io.github.jiashunx.masker.rest.framework.handler.*;
import io.github.jiashunx.masker.rest.framework.type.MRestNettyThreadType;
import io.github.jiashunx.masker.rest.framework.util.MRestHeaderBuilder;
import io.github.jiashunx.masker.rest.framework.util.MRestThreadFactory;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author jiashunx
 */
public class MRestServer {

    private static final Logger logger = LoggerFactory.getLogger(MRestServer.class);

    private volatile boolean started = false;

    private int listenPort;
    private String serverName;
    private int bossThreadNum = 0;
    private int workerThreadNum = 0;
    private boolean connectionKeepAlive;

    public MRestServer() {
        this(MRestUtils.getDefaultServerPort(), MRestUtils.getDefaultServerName());
    }

    public MRestServer(String serverName) {
        this(MRestUtils.getDefaultServerPort(), serverName);
    }

    public MRestServer(int listenPort) {
        this(listenPort, MRestUtils.getDefaultServerName());
    }

    public MRestServer(int listenPort, String serverName) {
        listenPort(listenPort);
        serverName(serverName);
    }

    public MRestServer listenPort(int listenPort) {
        if (listenPort <= 0 || listenPort > 65535) {
            throw new IllegalArgumentException("listenPort -> " + listenPort);
        }
        this.listenPort = listenPort;
        return this;
    }

    public MRestServer serverName(String serverName) {
        if (StringUtils.isBlank(serverName)) {
            throw new IllegalArgumentException("serverName -> " + serverName);
        }
        this.serverName = serverName;
        return this;
    }

    public MRestServer bossThreadNum(int bossThreadNum) {
        if (bossThreadNum < 0) {
            throw new IllegalArgumentException("bossThreadNum -> " + bossThreadNum);
        }
        this.bossThreadNum = bossThreadNum;
        return this;
    }

    public MRestServer workerThreadNum(int workerThreadNum) {
        if (workerThreadNum < 0) {
            throw new IllegalArgumentException("workThreadNum -> " + workerThreadNum);
        }
        this.workerThreadNum = workerThreadNum;
        return this;
    }

    public MRestServer connectionKeepAlive(boolean connectionKeepAlive) {
        this.connectionKeepAlive = connectionKeepAlive;
        return this;
    }

    public boolean isConnectionKeepAlive() {
        return this.connectionKeepAlive;
    }

    /**
     * 检查server是否已启动
     * @throws MRestServerInitializeException MRestServerInitializeException
     */
    private void checkServerState() throws MRestServerInitializeException {
        if (started) {
            throw new MRestServerInitializeException(String.format("server: %s has already been initialized", serverName));
        }
    }

    /**
     * 启动server
     * @throws MRestServerInitializeException MRestServerInitializeException
     */
    public synchronized void start() throws MRestServerInitializeException {
        checkServerState();
        if (logger.isInfoEnabled()) {
            logger.info("start server: {}, listening on port: {}", serverName, listenPort);
        }
        try {
            // mapping处理
            for (Runnable mappingTask: mappingTaskList) {
                mappingTask.run();
            }
            // filter处理
            for (Runnable filterTask: filterTaskList) {
                filterTask.run();
            }
            EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadNum, new MRestThreadFactory(MRestNettyThreadType.BOSS, listenPort));
            EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadNum, new MRestThreadFactory(MRestNettyThreadType.WORKER, listenPort));
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new MRestServerChannelInitializer(this));
            Channel channel = bootstrap.bind(listenPort).sync().channel();
            if (logger.isInfoEnabled()) {
                logger.info("start server: {} success, listening on port: {}", serverName, listenPort);
            }
            final Thread syncThread = new Thread(() -> {
                try {
                    channel.closeFuture().syncUninterruptibly();
                } catch (Throwable throwable) {
                    if (logger.isErrorEnabled()) {
                        logger.error("server: {} channel close future synchronized failed", serverName, throwable);
                    }
                }
            });
            syncThread.setName(serverName + "-closeFuture.Sync");
            syncThread.setDaemon(true);
            syncThread.start();
            started = true;
        } catch (Throwable throwable) {
            throw new MRestServerInitializeException(String.format("start server: %s failed", serverName), throwable);
        }
    }


    /**************************************************** SEP ****************************************************/
    /**************************************************** SEP ****************************************************/


    /**
     * 输入MRestRequest, 返回自定义对象.
     */
    private final Map<String, MRestHandler0<MRestRequest, ?>> functionHandlerMap = new HashMap<>();
    /**
     * 输入MRestRequest, 无返回.
     */
    private final Map<String, MRestHandler1<MRestRequest>> consumerHandlerMap1 = new HashMap<>();
    /**
     * 输入MRestRequest, MRestResponse, 无返回.
     */
    private final Map<String, MRestHandler2<MRestRequest, MRestResponse>> consumerHandlerMap2 = new HashMap<>();
    /**
     * 已添加映射的url列表.
     */
    private final Set<String> mappingUrlSet = new HashSet<>();
    /**
     * 添加url映射处理的任务(在服务启动时统一添加).
     */
    private final List<Runnable> mappingTaskList = new ArrayList<>();

    /**
     * 指定url是否是已指定映射处理.
     * @param requestURL requestURL
     * @return boolean
     */
    public boolean isMappingURL(String requestURL) {
        return mappingUrlSet.contains(requestURL);
    }

    /**
     * 检查映射url正确性.
     * @param url url
     */
    private void checkMappingUrl(String url) {
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException(String.format("illegal mapping url: %s", url));
        }
        if (isMappingURL(url)) {
            throw new MRestServerInitializeException(String.format("url mapping conflict: %s", url));
        }
    }

    public MRestServer mapping(String url, Consumer<MRestRequest> handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public MRestServer mapping(String url, Consumer<MRestRequest> handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public synchronized MRestServer mapping(String url, Consumer<MRestRequest> handler, MRestHandlerConfig config, HttpMethod... methods) {
        checkServerState();
        mappingTaskList.add(() -> {
            checkMappingUrl(url);
            MRestHandler1<MRestRequest> restHandler = new MRestHandler1<>(url, handler, config, methods);
            consumerHandlerMap1.put(url, restHandler);
            mappingUrlSet.add(url);
            if (logger.isInfoEnabled()) {
                logger.info("server: {} register url handler success, {}, {}", serverName, methods, url);
            }
        });
        return this;
    }

    public MRestServer mapping(String url, BiConsumer<MRestRequest, MRestResponse> handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public MRestServer mapping(String url, BiConsumer<MRestRequest, MRestResponse> handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public synchronized MRestServer mapping(String url, BiConsumer<MRestRequest, MRestResponse> handler, MRestHandlerConfig config, HttpMethod... methods) {
        checkServerState();
        mappingTaskList.add(() -> {
            checkMappingUrl(url);
            MRestHandler2<MRestRequest, MRestResponse> restHandler = new MRestHandler2<>(url, handler, config, methods);
            consumerHandlerMap2.put(url, restHandler);
            mappingUrlSet.add(url);
            if (logger.isInfoEnabled()) {
                logger.info("server: {} register url handler success, {}, {}", serverName, methods, url);
            }
        });
        return this;
    }

    public <R> MRestServer mapping(String url, Function<MRestRequest, R> handler, HttpMethod... methods) {
        return mapping(url, handler, MRestHeaderBuilder.Build(), methods);
    }

    public <R> MRestServer mapping(String url, Function<MRestRequest, R> handler, Map<String, Object> headers, HttpMethod... methods) {
        return mapping(url, handler, MRestHandlerConfig.newInstance(headers), methods);
    }

    public synchronized <R> MRestServer mapping(String url, Function<MRestRequest, R> handler, MRestHandlerConfig config, HttpMethod... methods) {
        checkServerState();
        mappingTaskList.add(() -> {
            checkMappingUrl(url);
            MRestHandler0<MRestRequest, R> restHandler = new MRestHandler0<>(url, handler, config, methods);
            functionHandlerMap.put(url, restHandler);
            mappingUrlSet.add(url);
            if (logger.isInfoEnabled()) {
                logger.info("server: {} register url handler success, {}, {}", serverName, methods, url);
            }
        });
        return this;
    }

    public MRestServer get(String url, Consumer<MRestRequest> handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public MRestServer get(String url, Consumer<MRestRequest> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public MRestServer get(String url, Consumer<MRestRequest> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.GET);
    }

    public <R> MRestServer get(String url, Function<MRestRequest, R> handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public <R> MRestServer get(String url, Function<MRestRequest, R> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public <R> MRestServer get(String url, Function<MRestRequest, R> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.GET);
    }

    public MRestServer get(String url, BiConsumer<MRestRequest, MRestResponse> handler) {
        return mapping(url, handler, HttpMethod.GET);
    }

    public MRestServer get(String url, BiConsumer<MRestRequest, MRestResponse> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.GET);
    }

    public MRestServer get(String url, BiConsumer<MRestRequest, MRestResponse> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.GET);
    }

    public MRestServer post(String url, Consumer<MRestRequest> handler) {
        return mapping(url, handler, HttpMethod.POST);
    }

    public MRestServer post(String url, Consumer<MRestRequest> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.POST);
    }

    public MRestServer post(String url, Consumer<MRestRequest> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.POST);
    }

    public <R> MRestServer post(String url, Function<MRestRequest, R> handler) {
        return mapping(url, handler, HttpMethod.POST);
    }

    public <R> MRestServer post(String url, Function<MRestRequest, R> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.POST);
    }

    public <R> MRestServer post(String url, Function<MRestRequest, R> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.POST);
    }

    public MRestServer post(String url, BiConsumer<MRestRequest, MRestResponse> handler) {
        return mapping(url, handler, HttpMethod.POST);
    }

    public MRestServer post(String url, BiConsumer<MRestRequest, MRestResponse> handler, Map<String, Object> headers) {
        return mapping(url, handler, headers, HttpMethod.POST);
    }

    public MRestServer post(String url, BiConsumer<MRestRequest, MRestResponse> handler, MRestHandlerConfig config) {
        return mapping(url, handler, config, HttpMethod.POST);
    }

    public MRestHandler1<MRestRequest> getConsumerHandler1(String requestURL) {
        return consumerHandlerMap1.get(requestURL);
    }

    public MRestHandler2<MRestRequest, MRestResponse> getConsumerHandler2(String requestURL) {
        return consumerHandlerMap2.get(requestURL);
    }

    public MRestHandler0<MRestRequest, ?> getFunctionHandler(String requestURL) {
        return functionHandlerMap.get(requestURL);
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
     * 添加filter的任务(在服务启动时统一添加).
     */
    private final List<Runnable> filterTaskList = new ArrayList<>();

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
            return order1 - order0;
        });
        filterList.addLast(requestFilter);
        return new MRestFilterChain(this, filterList.toArray(new MRestFilter[0]));
    }

    /**
     * 添加filter, 自动扫描filter注解, 获取urlPattern.
     * @param filterArr filterArr
     * @return MRestServer
     */
    public synchronized MRestServer filter(MRestFilter... filterArr) {
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
     * 添加filter, 如果filter未配置注解, 则根据传入urlPattern来进行匹配, 否则将注解指定的urlPattern和urlPattern参数进行合并.
     * @param urlPattern urlPattern
     * @param filterArr filterArr
     * @return MRestServer
     */
    public synchronized MRestServer filter(String urlPattern, MRestFilter... filterArr) {
        for (MRestFilter filter: filterArr) {
            Set<String> urlPatterns = new HashSet<>(Arrays.asList(filter.urlPatterns()));
            if (!filter.hasFilterAnnotation()) {
                urlPatterns.clear();
            }
            urlPatterns.add(urlPattern);
            filter0(filter, urlPatterns.toArray(new String[0]));
        }
        return this;
    }

    private synchronized void filter0(MRestFilter filter, String... urlPatterns) {
        checkServerState();
        filterTaskList.add(() -> {
            MRestFilter restFilter = Objects.requireNonNull(filter);
            if (urlPatterns.length == 0) {
                throw new IllegalArgumentException("can't assign empty urlPatterns to filter: " + filter.filterName());
            }
            for (String urlPattern: urlPatterns) {
                filterMap.computeIfAbsent(urlPattern, k -> new ArrayList<>()).add(restFilter);
                if (logger.isInfoEnabled()) {
                    logger.info("server: {} register filter success, {} -> {}", serverName, urlPattern, filter.filterName());
                }
            }
        });
    }


    /**************************************************** SEP ****************************************************/
    /**************************************************** SEP ****************************************************/

}

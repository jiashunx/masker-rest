package io.github.jiashunx.masker.rest.framework;

import io.github.jiashunx.masker.rest.framework.cons.Constants;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerCloseException;
import io.github.jiashunx.masker.rest.framework.exception.MRestServerInitializeException;
import io.github.jiashunx.masker.rest.framework.function.VoidFunc;
import io.github.jiashunx.masker.rest.framework.handler.*;
import io.github.jiashunx.masker.rest.framework.model.MRestServerConfig;
import io.github.jiashunx.masker.rest.framework.type.MRestNettyThreadType;
import io.github.jiashunx.masker.rest.framework.util.MRestThreadFactory;
import io.github.jiashunx.masker.rest.framework.util.MRestUtils;
import io.github.jiashunx.masker.rest.framework.util.StringUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jiashunx
 */
public class MRestServer {

    private static final Logger logger = LoggerFactory.getLogger(MRestServer.class);

    private volatile boolean started = false;
    private volatile boolean closed = false;
    private final String startupTime;
    private final String identifier;

    private Channel serverChannel;

    private int listenPort;
    private String serverName;
    private int bossThreadNum;
    private int workerThreadNum;
    private boolean connectionKeepAlive;
    /**
     * http请求报文字节大小限制
     */
    private int httpContentMaxByteSize;

    /**
     * 是否开启Https
     */
    private boolean sslEnabled;

    /**
     * SSL配置
     */
    private SslContext sslContext;

    private final Map<String, MRestContext> contextMap = new ConcurrentHashMap<>();

    private final Map<String, Object> globalObjects = new ConcurrentHashMap<>();

    private final List<VoidFunc> callbackAfterStartups = new ArrayList<>();

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
        MRestServerConfig defaultServerConfig = MRestUtils.getDefaultServerConfig();
        bossThreadNum(defaultServerConfig.getBossThreadNum());
        workerThreadNum(defaultServerConfig.getWorkerThreadNum());
        connectionKeepAlive(defaultServerConfig.isConnectionKeepAlive());
        httpContentMaxMBSize(defaultServerConfig.getHttpContentMaxMBSize());
        this.startupTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
        this.identifier = UUID.randomUUID().toString().replace("-", "");
    }

    public String getStartupTime() {
        return startupTime;
    }

    public String getIdentifier() {
        return identifier;
    }

    public MRestServer listenPort(int listenPort) {
        if (listenPort <= 0 || listenPort > 65535) {
            throw new IllegalArgumentException("listenPort -> " + listenPort);
        }
        this.listenPort = listenPort;
        return this;
    }

    public int getListenPort() {
        return listenPort;
    }

    public MRestServer serverName(String serverName) {
        if (StringUtils.isEmpty(serverName)) {
            throw new IllegalArgumentException("serverName -> " + serverName);
        }
        this.serverName = serverName;
        return this;
    }

    public String getServerName() {
        return serverName;
    }

    public MRestServer bossThreadNum(int bossThreadNum) {
        if (bossThreadNum <= 0) {
            throw new IllegalArgumentException("bossThreadNum -> " + bossThreadNum);
        }
        this.bossThreadNum = bossThreadNum;
        return this;
    }

    public int getBossThreadNum() {
        return bossThreadNum;
    }

    public MRestServer workerThreadNum(int workerThreadNum) {
        if (workerThreadNum <= 0) {
            throw new IllegalArgumentException("workThreadNum -> " + workerThreadNum);
        }
        this.workerThreadNum = workerThreadNum;
        return this;
    }

    public int getWorkerThreadNum() {
        return workerThreadNum;
    }

    @Deprecated
    public MRestServer httpContentMaxLength(int httpContentMaxLength) {
        return this.httpContentMaxByteSize(httpContentMaxLength);
    }

    @Deprecated
    public int getHttpContentMaxLength() {
        return this.getHttpContentMaxByteSize();
    }

    public MRestServer httpContentMaxByteSize(int httpContentMaxByteSize) {
        if (httpContentMaxByteSize <= 0) {
            throw new IllegalArgumentException("httpContentMaxByteSize must large than zero -> " + httpContentMaxByteSize);
        }
        this.httpContentMaxByteSize = httpContentMaxByteSize;
        return this;
    }

    public MRestServer httpContentMaxKBSize(int httpContentMaxKBSize) {
        if (httpContentMaxKBSize <= 0) {
            throw new IllegalArgumentException("httpContentMaxKBSize must large than zero -> " + httpContentMaxKBSize);
        }
        return this.httpContentMaxByteSize(httpContentMaxKBSize * 1024);
    }

    public MRestServer httpContentMaxMBSize(int httpContentMaxMBSize) {
        if (httpContentMaxMBSize <= 0) {
            throw new IllegalArgumentException("httpContentMaxMBSize must large than zero -> " + httpContentMaxMBSize);
        }
        return this.httpContentMaxKBSize(httpContentMaxMBSize * 1024);
    }

    public int getHttpContentMaxByteSize() {
        return this.httpContentMaxByteSize;
    }

    public MRestServer sslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
        return this;
    }

    public boolean isSslEnabled() {
        return this.sslEnabled;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public void setSslContext(SslContext sslContext) {
        this.sslContext = sslContext;
    }

    public MRestServer connectionKeepAlive(boolean connectionKeepAlive) {
        this.connectionKeepAlive = connectionKeepAlive;
        return this;
    }

    public boolean isConnectionKeepAlive() {
        return this.connectionKeepAlive;
    }

    public MRestContext context() {
        return context(Constants.DEFAULT_CONTEXT_PATH);
    }

    public synchronized MRestContext context(String contextPath) {
        MRestContext context = getContext(contextPath);
        if (context == null) {
            String _ctxPath = MRestUtils.formatContextPath(contextPath);
            context = new MRestContext(this, _ctxPath);
            contextMap.put(_ctxPath, context);
        }
        return context;
    }

    public MRestContext getContext(String contextPath) {
        return contextMap.get(MRestUtils.formatContextPath(contextPath));
    }

    public List<String> getContextList() {
        return new ArrayList<>(contextMap.keySet());
    }

    public String getServerDesc() {
        return String.format("Server[%s:%d]", getServerName(), getListenPort());
    }

    /**
     * 检查server是否已关闭或已启动
     * @throws MRestServerInitializeException MRestServerInitializeException
     */
    public void checkServerState() throws MRestServerInitializeException {
        if (closed) {
            throw new MRestServerCloseException(String.format("%s has already been closed", getServerDesc()));
        }
        if (started) {
            throw new MRestServerInitializeException(String.format("%s has already been initialized", getServerDesc()));
        }
    }

    private synchronized void initSslContext() throws MRestServerInitializeException {
        if (this.isSslEnabled() || this.sslContext != null) {
            SslContext sslContext = this.sslContext;
            if (sslContext == null) {
                logger.info("{} 未指定SslContext, 加载默认SSL配置", getServerDesc());
                try (InputStream serverCert = MRestServer.class.getClassLoader().getResourceAsStream("masker-rest/ssl/server.crt");
                     InputStream serverKey = MRestServer.class.getClassLoader().getResourceAsStream("masker-rest/ssl/server_pkcs8.key");
                     InputStream caCert = MRestServer.class.getClassLoader().getResourceAsStream("masker-rest/ssl/ca.crt");) {
                    sslContext = SslContextBuilder.forServer(serverCert, serverKey).trustManager(caCert).clientAuth(ClientAuth.REQUIRE).build();
                } catch (Throwable throwable) {
                    throw new MRestServerInitializeException("SslContext initialize failed", throwable);
                }
            }
            this.sslEnabled = true;
            this.sslContext = sslContext;
        }
    }

    public synchronized MRestServer shutdown() {
        if (!started) {
            throw new MRestServerCloseException(String.format("%s has not been initialized", getServerDesc()));
        }
        if (closed) {
            throw new MRestServerCloseException(String.format("%s has already been closed", getServerDesc()));
        }
        try {
            serverChannel.close().addListener(future -> {
                logger.info("{} closed", getServerDesc());
            }).get();
        } catch (Throwable throwable) {
            throw new MRestServerCloseException(String.format("%s close failed", getServerDesc()), throwable);
        }
        closed = true;
        serverChannel = null;
        return this;
    }

    /**
     * 启动server
     * @return MRestServer server实例
     * @throws MRestServerInitializeException MRestServerInitializeException
     */
    public synchronized MRestServer start() throws MRestServerInitializeException {
        try {
            // 检查Server状态
            checkServerState();
            // 初始化SslContext
            initSslContext();
            // 添加默认Context
            if (getContext(Constants.DEFAULT_CONTEXT_PATH) == null) {
                contextMap.put(Constants.DEFAULT_CONTEXT_PATH, new MRestContext(this, Constants.DEFAULT_CONTEXT_PATH));
            }
            // Context初始化资源
            contextMap.forEach((key, restContext) -> {
                restContext.initResources();
            });
            logger.info("{} starting, Context: {}", getServerDesc(), getContextList());
            // Context初始化
            contextMap.forEach((key, restContext) -> {
                restContext.init();
            });
            EventLoopGroup bossGroup = new NioEventLoopGroup(bossThreadNum, new MRestThreadFactory(MRestNettyThreadType.BOSS, listenPort));
            EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadNum, new MRestThreadFactory(MRestNettyThreadType.WORKER, listenPort));
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new MRestServerChannelInitializer(this));
            serverChannel = bootstrap.bind(listenPort).sync().channel();
            logger.info("{} started", getServerDesc());
            AtomicReference<Channel> serverChannelRef = new AtomicReference<>(serverChannel);
            final Thread syncThread = new Thread(() -> {
                try {
                    serverChannelRef.get().closeFuture().syncUninterruptibly();
                } catch (Throwable throwable) {
                    logger.error("{} channel close future synchronized failed", getServerDesc(), throwable);
                } finally {
                    serverChannelRef.set(null);
                }
            });
            syncThread.setName(getServerDesc() + "-closeFuture.Sync");
            syncThread.setDaemon(true);
            syncThread.start();
            started = true;
            if (!this.callbackAfterStartups.isEmpty()) {
                for (int index = 0, size = this.callbackAfterStartups.size(); index < size; index++) {
                    VoidFunc callbackAfterStartup = this.callbackAfterStartups.get(index);
                    if (callbackAfterStartup != null) {
                        try {
                            callbackAfterStartup.doSomething();
                        } catch (Throwable throwable) {
                            logger.error("{} callbackAfterStartup execute failed, index=[{}]", getServerDesc(), index, throwable);
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            throw new MRestServerInitializeException(String.format("%s start failed", getServerDesc()), throwable);
        }
        return this;
    }

    public MRestServer setGlobalObject(String key, Object object) {
        globalObjects.put(key, object);
        return this;
    }

    public Object getGlobalObject(String key) {
        return globalObjects.get(key);
    }

    public MRestServer callbackAfterStartup(VoidFunc... callbackAfterStartupArr) {
        this.callbackAfterStartups.addAll(Arrays.asList(callbackAfterStartupArr));
        return this;
    }

    public List<VoidFunc> getCallbackAfterStartups() {
        return callbackAfterStartups;
    }

}

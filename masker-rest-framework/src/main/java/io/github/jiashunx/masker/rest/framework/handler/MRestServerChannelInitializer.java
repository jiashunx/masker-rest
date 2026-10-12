package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import javax.net.ssl.SSLEngine;
import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final MRestServer restServer;

    public MRestServerChannelInitializer(MRestServer restServer) {
        this.restServer = Objects.requireNonNull(restServer);
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        // 注册子通道, 便于server关闭时统一关闭所有活跃连接.
        this.restServer.getChildChannels().add(socketChannel);
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (this.restServer.isSslEnabled()) {
            SSLEngine sslEngine = this.restServer.getSslContext().newEngine(socketChannel.alloc());
            sslEngine.setUseClientMode(false);
            sslEngine.setNeedClientAuth(this.restServer.isSslNeedClientAuth());
            pipeline.addFirst("ssl", new SslHandler(sslEngine));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        // 聚合Http请求或响应，否则会收到HttpMessage，HttpContent等对象
        // 使用此Handler后, 只会收到FullHttpRequest等对象
        pipeline.addLast(new HttpObjectAggregator(restServer.getHttpContentMaxByteSize()));
        pipeline.addLast(new HttpServerExpectContinueHandler());
        // 可选的连接空闲超时(默认0=不启用), 用于缓解慢连接攻击.
        if (restServer.getHttpIdleTimeoutSeconds() > 0) {
            pipeline.addLast(new IdleStateHandler(0, 0, restServer.getHttpIdleTimeoutSeconds()));
        }
        if (restServer.getBusinessGroup() != null) {
            // 使用独立业务线程组执行请求处理, 避免阻塞IO/CPU占用worker线程.
            pipeline.addLast(restServer.getBusinessGroup(), new MRestServerChannelHandler(restServer));
        } else {
            pipeline.addLast(new MRestServerChannelHandler(restServer));
        }
    }

}

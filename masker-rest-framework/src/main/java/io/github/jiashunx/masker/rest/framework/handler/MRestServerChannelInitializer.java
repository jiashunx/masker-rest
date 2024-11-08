package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.Objects;

/**
 * @author jiashunx
 */
public class MRestServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final MRestServer restServer;

    private final SslContext sslContext;

    public MRestServerChannelInitializer(MRestServer restServer) {
        this(restServer, null);
    }

    public MRestServerChannelInitializer(MRestServer restServer, SslContext sslContext) {
        this.restServer = Objects.requireNonNull(restServer);
        this.sslContext = sslContext;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        if (this.sslContext != null) {
            pipeline.addFirst("ssl", new SslHandler(sslContext.newEngine(socketChannel.alloc())));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        // 聚合Http请求或响应，否则会收到HttpMessage，HttpContent等对象
        // 使用此Handler后, 只会收到FullHttpRequest等对象
        pipeline.addLast(new HttpObjectAggregator(restServer.getHttpContentMaxByteSize()));
        pipeline.addLast(new HttpServerExpectContinueHandler());
        pipeline.addLast(new MRestServerChannelHandler(restServer));
    }

}

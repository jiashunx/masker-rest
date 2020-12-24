package io.github.jiashunx.masker.rest.framework.handler;

import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

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
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        // 聚合Http请求或响应，否则会收到HttpMessage，HttpContent等对象
        // 使用此Handler后, 只会收到FullHttpRequest等对象
        pipeline.addLast(new HttpObjectAggregator(50*1024*1024));
        pipeline.addLast(new HttpServerExpectContinueHandler());
        pipeline.addLast(new MRestServerChannelHandler(this.restServer));
    }

}

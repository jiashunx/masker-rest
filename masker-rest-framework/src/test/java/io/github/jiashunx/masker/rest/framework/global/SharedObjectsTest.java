package io.github.jiashunx.masker.rest.framework.global;

import static org.junit.Assert.*;
import org.junit.Test;

import io.github.jiashunx.masker.rest.framework.MRestContext;
import io.github.jiashunx.masker.rest.framework.MRestRequest;
import io.github.jiashunx.masker.rest.framework.MRestResponse;
import io.github.jiashunx.masker.rest.framework.MRestServer;
import io.github.jiashunx.masker.rest.framework.model.MRestServerThreadModel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;

public class SharedObjectsTest {

    @Test
    public void test_propagateAndRestore() {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        try {
            ChannelHandlerContext ctx = channel.pipeline().firstContext();
            MRestContext context = new MRestServer(8090, "shared-obj-server").context();
            MRestServerThreadModel model = new MRestServerThreadModel();
            model.setRestContext(context);
            model.setRestRequest(new MRestRequest());
            model.setRestResponse(new MRestResponse(ctx, context));

            assertNull(SharedObjects.getServerThreadModel());
            SharedObjects.runWithServerThreadModel(model, () -> assertSame(model, SharedObjects.getServerThreadModel()));
            assertNull("context should be restored to null after run", SharedObjects.getServerThreadModel());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

}

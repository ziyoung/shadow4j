package net.ziyoung.shadow4j.shadow;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

@ChannelHandler.Sharable
public class ChannelIdleHandler extends ChannelInboundHandlerAdapter {

    public static final ChannelIdleHandler INSTANCE = new ChannelIdleHandler();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            ShadowUtil.closeChannelOnFlush(ctx.channel());
        } else {
            ctx.fireUserEventTriggered(evt);
        }
    }

}

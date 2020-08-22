package net.ziyoung.shadow4j.shadow;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DirectHandler extends ChannelInboundHandlerAdapter {

    private final Promise<Channel> promise;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().remove(this);
        promise.setSuccess(ctx.channel());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        promise.setFailure(cause);
    }

}

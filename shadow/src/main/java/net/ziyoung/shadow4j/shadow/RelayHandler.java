package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RelayHandler extends ChannelInboundHandlerAdapter {

    private final Channel outBoundChannel;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        outBoundChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (outBoundChannel.isActive()) {
            outBoundChannel.writeAndFlush(msg);
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelUtils.closeOnFlush(outBoundChannel);
    }

}

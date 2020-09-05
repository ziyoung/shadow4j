package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class RelayHandler extends ChannelInboundHandlerAdapter {

    private final Channel outBoundChannel;
    private final boolean useRaw;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        outBoundChannel.writeAndFlush(Unpooled.EMPTY_BUFFER);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (outBoundChannel.isActive()) {
            if (useRaw) {
                ChannelFuture future = outBoundChannel.writeAndFlush(msg);
                future.addListener((ChannelFutureListener) future1 -> {
                    if (!future1.isSuccess()) {
                        log.error("relay handler write error", future1.cause());
                        outBoundChannel.close();
                    }
                });
            } else {
                ByteBuf byteBuf = (ByteBuf) msg;
                byte[] bytes = new byte[byteBuf.readableBytes()];
                byteBuf.readBytes(bytes);
                outBoundChannel.writeAndFlush(new ShadowStream(bytes));
            }
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ShadowUtils.closeChannelOnFlush(outBoundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(ctx.channel().toString() + " error:", cause);
        ctx.close();
        ShadowUtils.closeChannelOnFlush(outBoundChannel);
    }

}

package net.ziyoung.shadow4j.shadow;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelDuplexHandler {

    public static final ExceptionHandler INSTANCE = new ExceptionHandler();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ShadowUtils.closeChannelOnFlush(ctx.channel());
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        promise.addListener((ChannelFutureListener) future -> {
            if (!future.isSuccess()) {
                log.error("write error", future.cause());
                ShadowUtils.closeChannelOnFlush(future.channel());
            }
        });
    }

}

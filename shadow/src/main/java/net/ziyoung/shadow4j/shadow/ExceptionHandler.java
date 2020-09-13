package net.ziyoung.shadow4j.shadow;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ExceptionHandler extends ChannelDuplexHandler {

    public static final ExceptionHandler INSTANCE = new ExceptionHandler();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exception", cause);
        ShadowUtil.closeChannelOnFlush(ctx.channel());
    }

//    @Override
//    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
//        promise.addListener((ChannelFutureListener) future -> {
//            if (!future.isSuccess()) {
//                log.error("write error", future.cause());
//                ShadowUtil.closeChannelOnFlush(future.channel());
//            }
//        });
//    }

}

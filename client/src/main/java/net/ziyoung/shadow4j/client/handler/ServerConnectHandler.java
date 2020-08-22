package net.ziyoung.shadow4j.client.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ziyoung.shadow4j.shadow.ShadowUtils;
import net.ziyoung.shadow4j.shadow.ShadowStream;

import java.net.SocketAddress;

@Slf4j
@AllArgsConstructor
public class ServerConnectHandler extends SimpleChannelInboundHandler<ShadowStream> {

    private final SocketAddress server;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ShadowStream shadowStream) throws Exception {
        Bootstrap bootstrap = new Bootstrap();

        Promise<Channel> promise = ctx.executor().newPromise();
        promise.addListener((FutureListener<Channel>) future -> {
            if (future.isSuccess()) {
                Channel channel = future.getNow();
                channel.writeAndFlush(shadowStream);
            } else {
                ShadowUtils.closeChannelOnFlush(ctx.channel());
            }
        });

        Channel inboundChannel = ctx.channel();
        bootstrap.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new ServerConnectInitializer(null, promise));
        bootstrap.connect(server).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                // log.info("proxy {} <-> {} <-> {}", local, server, target);
                log.info("proxy {} <-> {}", server, shadowStream);
            } else {
                ShadowUtils.closeChannelOnFlush(inboundChannel);
            }
        });
    }

}

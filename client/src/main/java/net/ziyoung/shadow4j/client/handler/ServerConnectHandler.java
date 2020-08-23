package net.ziyoung.shadow4j.client.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ziyoung.shadow4j.shadow.ShadowConfig;
import net.ziyoung.shadow4j.shadow.ShadowStream;
import net.ziyoung.shadow4j.shadow.ShadowUtils;

import java.net.InetSocketAddress;

@Slf4j
@AllArgsConstructor
public class ServerConnectHandler extends SimpleChannelInboundHandler<ShadowStream> {

    private final ShadowConfig config;

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
                .handler(new ServerConnectInitializer(config, promise));
        InetSocketAddress server = config.getServer();
        bootstrap.connect(server.getAddress(), server.getPort())
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("proxy {} <-> {}", shadowStream, server);
                    } else {
                        log.error("unable to connect server {}", server);
                        ShadowUtils.closeChannelOnFlush(inboundChannel);
                    }
                });
    }

}

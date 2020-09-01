package net.ziyoung.shadow4j.client.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ziyoung.shadow4j.shadow.*;

import java.net.InetSocketAddress;

@Slf4j
@AllArgsConstructor
public class ServerConnectHandler extends SimpleChannelInboundHandler<SocksAddress> {

    private final ShadowConfig config;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksAddress address) throws Exception {
        Bootstrap bootstrap = new Bootstrap();

        Promise<Channel> promise = ctx.executor().newPromise();
        promise.addListener((FutureListener<Channel>) future -> {
            if (future.isSuccess()) {
                Channel outboundChannel = future.getNow();
                log.debug("start to write address '{}' to server", address);
                log.debug("channel is ==> {}", outboundChannel);
                ChannelFuture responseFuture = outboundChannel.writeAndFlush(address);
                responseFuture.addListener((ChannelFutureListener) future1 -> {
                    log.debug("write is success");
                    if (future1.isSuccess()) {
                        // handshake is done, so we flush message to inform SOCK5 client
                        ctx.writeAndFlush(Unpooled.EMPTY_BUFFER);

                        log.debug("start to add relay handler");
                        ctx.pipeline().replace(ctx.name(), null, new RelayHandler(outboundChannel, false));
                        outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel(), true));
//                        ctx.pipeline().addLast(new RelayHandler(outboundChannel));
                    } else {
                        log.error("send address error", future1.cause());
                        ShadowUtils.closeChannelOnFlush(ctx.channel());
                    }
                });
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
                        log.info("proxy {} <-> {} <-> {}", future.channel().localAddress(), server, address);
                    } else {
                        log.error("unable to connect server {}", server);
                        ShadowUtils.closeChannelOnFlush(inboundChannel);
                    }
                });
    }

}

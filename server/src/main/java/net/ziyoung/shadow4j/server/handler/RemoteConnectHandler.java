package net.ziyoung.shadow4j.server.handler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;
import lombok.extern.slf4j.Slf4j;
import net.ziyoung.shadow4j.shadow.*;

import java.util.LinkedList;
import java.util.Queue;

@Slf4j
public class RemoteConnectHandler extends SimpleChannelInboundHandler<ShadowStream> {

    private boolean isConnecting = false;
    private final Queue<ShadowStream> cache = new LinkedList<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ShadowStream stream) throws Exception {
        if (isConnecting) {
            cache.add(stream);
        } else {
            isConnecting = true;
//            ShadowAddress address = (ShadowAddress) stream;
            ShadowAddress address = new ShadowAddress(stream.getData());
            connect(ctx, address);
        }
    }

    private void connect(ChannelHandlerContext ctx, ShadowAddress address) {
        Bootstrap bootstrap = new Bootstrap();
        Promise<Channel> promise = ctx.executor().newPromise();
        Channel inboundChannel = ctx.channel();

        promise.addListener((FutureListener<Channel>) future -> {
            if (future.isSuccess()) {
                Channel outboundChannel = future.getNow();
                log.debug("write cache to outbound channel");
                flushCache(outboundChannel);

                log.debug("start to add relay handler");
                ctx.pipeline().addAfter(ctx.name(), null, new RelayHandler(outboundChannel, true));
                ctx.pipeline().remove(this);
                outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel(), false));
            } else {
                ShadowUtil.closeChannelOnFlush(ctx.channel());
            }
        });

        bootstrap.group(inboundChannel.eventLoop())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .handler(new RemoteConnectInitializer(promise));
        bootstrap.connect(address.getHost(), address.getPort())
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        log.info("proxy {} <-> {}", future.channel().localAddress(), address);
                    } else {
                        log.error("unable to connect remote {}", address);
                        ShadowUtil.closeChannelOnFlush(inboundChannel);
                    }
                });
    }

    private void flushCache(Channel channel) {
        if (cache.size() == 0) {
            return;
        }

        ShadowStream stream = cache.poll();
        while (stream != null) {
            channel.write(stream).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    log.warn("cause ", future.cause());
                }
            });
            stream = cache.poll();
        }
        channel.flush();
    }

}

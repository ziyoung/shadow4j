package net.ziyoung.shadow4j.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;
import net.ziyoung.shadow4j.shadow.ChannelIdleHandler;
import net.ziyoung.shadow4j.shadow.CombinedShadowStreamCodec;
import net.ziyoung.shadow4j.shadow.DirectHandler;
import net.ziyoung.shadow4j.shadow.ShadowConfig;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class ServerConnectInitializer extends ChannelInitializer<SocketChannel> {

    private final ShadowConfig config;
    private final Promise<Channel> promise;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(new CombinedShadowStreamCodec(config));
        channel.pipeline().addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
        channel.pipeline().addLast(ChannelIdleHandler.INSTANCE);
        channel.pipeline().addLast(new DirectHandler(promise));
    }

}

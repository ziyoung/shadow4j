package net.ziyoung.shadow4j.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ziyoung.shadow4j.shadow.CombinedShadowStreamCodec;
import net.ziyoung.shadow4j.shadow.DirectHandler;
import net.ziyoung.shadow4j.shadow.ExceptionHandler;
import net.ziyoung.shadow4j.shadow.ShadowConfig;

@Slf4j
@AllArgsConstructor
public class ServerConnectInitializer extends ChannelInitializer<SocketChannel> {

    private final ShadowConfig config;
    private final Promise<Channel> promise;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(new CombinedShadowStreamCodec(config));
        channel.pipeline().addLast(new DirectHandler(promise));
        channel.pipeline().addLast(ExceptionHandler.INSTANCE);
        log.debug("channel pipeline names {}", channel.pipeline().names());
    }

}

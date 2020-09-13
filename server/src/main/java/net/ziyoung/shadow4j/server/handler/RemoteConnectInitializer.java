package net.ziyoung.shadow4j.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Promise;
import lombok.AllArgsConstructor;
import net.ziyoung.shadow4j.shadow.ChannelIdleHandler;
import net.ziyoung.shadow4j.shadow.DirectHandler;
import net.ziyoung.shadow4j.shadow.ExceptionHandler;
import net.ziyoung.shadow4j.shadow.ShadowStreamRawEncoder;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class RemoteConnectInitializer extends ChannelInitializer<SocketChannel> {

    private final Promise<Channel> promise;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        channel.pipeline().addLast(new LoggingHandler());
        channel.pipeline().addLast(ShadowStreamRawEncoder.INSTANCE);
        channel.pipeline().addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
        channel.pipeline().addLast(ChannelIdleHandler.INSTANCE);
        channel.pipeline().addLast(new DirectHandler(promise));
        channel.pipeline().addLast(ExceptionHandler.INSTANCE);
    }

}

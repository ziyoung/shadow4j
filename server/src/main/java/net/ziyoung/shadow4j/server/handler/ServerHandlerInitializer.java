package net.ziyoung.shadow4j.server.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;
import net.ziyoung.shadow4j.server.ServerConfig;
import net.ziyoung.shadow4j.shadow.ChannelIdleHandler;
import net.ziyoung.shadow4j.shadow.ExceptionHandler;
import net.ziyoung.shadow4j.shadow.ShadowStreamCodec;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private final ServerConfig config;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        LogLevel logLevel = config.isVerboseMode() ? LogLevel.DEBUG : LogLevel.INFO;
        channel.pipeline().addLast(new LoggingHandler(logLevel));
        channel.pipeline().addLast(new ShadowStreamCodec(config.getShadowConfig(), true));
        channel.pipeline().addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
        channel.pipeline().addLast(ChannelIdleHandler.INSTANCE);
        channel.pipeline().addLast(new RemoteConnectHandler());
        channel.pipeline().addLast(ExceptionHandler.INSTANCE);
    }

}

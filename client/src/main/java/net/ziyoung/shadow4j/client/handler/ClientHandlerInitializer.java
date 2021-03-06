package net.ziyoung.shadow4j.client.handler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.socksx.v5.Socks5InitialRequestDecoder;
import io.netty.handler.codec.socksx.v5.Socks5ServerEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;
import net.ziyoung.shadow4j.client.ClientConfig;
import net.ziyoung.shadow4j.shadow.ChannelIdleHandler;
import net.ziyoung.shadow4j.shadow.ShadowStreamRawEncoder;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class ClientHandlerInitializer extends ChannelInitializer<SocketChannel> {

    private final ClientConfig config;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        LogLevel logLevel = config.isVerboseMode() ? LogLevel.DEBUG : LogLevel.INFO;
        channel.pipeline().addLast(new LoggingHandler(logLevel));
        channel.pipeline().addLast(new Socks5InitialRequestDecoder());
        channel.pipeline().addLast(Socks5ServerEncoder.DEFAULT);
        // FIXME: add HttpResponseEncoder
//        channel.pipeline().addLast(new HttpResponseEncoder());
        channel.pipeline().addLast(ShadowStreamRawEncoder.INSTANCE);
        channel.pipeline().addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS));
        channel.pipeline().addLast(ChannelIdleHandler.INSTANCE);
        channel.pipeline().addLast(new ClientHandler(config));
    }

}

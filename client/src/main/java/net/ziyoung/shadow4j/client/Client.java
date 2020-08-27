package net.ziyoung.shadow4j.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.ziyoung.shadow4j.client.handler.ClientHandlerInitializer;

public class Client {

    private final ClientConfig config;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;

    public Client(ClientConfig config) {
        this.config = config;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }

    public void start() throws Exception {
        try {
            serveSocks();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void serveSocks() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .localAddress(config.getSocks().getPort())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ClientHandlerInitializer(config));
        ChannelFuture future = bootstrap.bind().sync();
        // TODO: not use sync().
        future.channel().closeFuture().sync();
    }

}

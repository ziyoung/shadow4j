package net.ziyoung.shadow4j.client;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

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
        serveSocks();
    }

    private void serveSocks() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .localAddress(config.getSocks().getPort())
                .channel(NioServerSocketChannel.class);
        ChannelFuture future = bootstrap.bind().sync();
        future.channel().closeFuture().sync();
    }

}

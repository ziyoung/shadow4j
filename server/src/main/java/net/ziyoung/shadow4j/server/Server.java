package net.ziyoung.shadow4j.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.ziyoung.shadow4j.server.handler.ServerHandlerInitializer;

import java.util.ArrayList;
import java.util.List;

public class Server {

    private final ServerConfig config;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;
    private final List<ChannelFuture> futureList;

    public Server(ServerConfig config) {
        this.config = config;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        this.futureList = new ArrayList<>();
    }

    public void start() throws Exception {
        try {
            serveSocks();

            for (ChannelFuture future : futureList) {
                future.sync();
            }
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void serveSocks() throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .localAddress(config.getPort())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ServerHandlerInitializer(config));

        ChannelFuture future = bootstrap.bind().sync();
        ChannelFuture closeFuture = future.channel().closeFuture();
        futureList.add(closeFuture);
    }

}

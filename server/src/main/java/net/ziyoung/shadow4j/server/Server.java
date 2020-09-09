package net.ziyoung.shadow4j.server;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;

public class Server {

    private final ServerConfig config;
    private final EventLoopGroup bossGroup;
    private final EventLoopGroup workerGroup;


    public Server(ServerConfig config) {
        this.config = config;
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
    }

    public void start() throws Exception {

    }

}

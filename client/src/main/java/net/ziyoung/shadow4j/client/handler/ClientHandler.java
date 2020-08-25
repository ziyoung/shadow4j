package net.ziyoung.shadow4j.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v5.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.ziyoung.shadow4j.client.ClientConfig;
import net.ziyoung.shadow4j.shadow.ShadowUtils;

import java.net.InetSocketAddress;

@Slf4j
@AllArgsConstructor
public class ClientHandler extends SimpleChannelInboundHandler<SocksMessage> {

    private final ClientConfig config;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SocksMessage socksMessage) throws Exception {
        if (socksMessage instanceof Socks5InitialRequest) {
            ctx.pipeline().remove(Socks5InitialRequestDecoder.class);
            ctx.pipeline().addFirst(new Socks5CommandRequestDecoder());
            ctx.write(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
        } else if (socksMessage instanceof Socks5CommandRequest) {
            Socks5CommandRequest request = (Socks5CommandRequest) socksMessage;
            Socks5CommandType type = request.type();

            if (type == Socks5CommandType.CONNECT) {
                ctx.write(new DefaultSocks5CommandResponse(Socks5CommandStatus.SUCCESS, Socks5AddressType.IPv4));
                ctx.pipeline().addAfter(ctx.name(), null, new ServerConnectHandler(config.getShadowConfig()));
                ctx.pipeline().remove(this);
            } else if (type == Socks5CommandType.UDP_ASSOCIATE) {
                if (!config.isUnpAssociate()) {
                    log.warn("unp associate is not supported, so this channel will be closed");
                    ShadowUtils.closeChannelOnFlush(ctx.channel());
                } else {
                    InetSocketAddress localAddress = (InetSocketAddress) ctx.channel().localAddress();
                    log.debug("unp associate is true and localAddress is {}", localAddress);
//                    SocksAddress socksAddress = SocksAddress.valueOf(localAddress.getHostName(), localAddress.getPort());
                    ctx.write(new DefaultSocks5CommandResponse(
                            Socks5CommandStatus.SUCCESS,
                            Socks5AddressType.IPv4,
                            localAddress.getHostName(),
                            localAddress.getPort()));
                    ctx.pipeline().addAfter(ctx.name(), null, new ServerConnectHandler(config.getShadowConfig()));
                    ctx.pipeline().remove(this);
                }
            } else {
                log.warn("unsupported command type {}", type);
                ctx.close();
            }
        }
    }

}

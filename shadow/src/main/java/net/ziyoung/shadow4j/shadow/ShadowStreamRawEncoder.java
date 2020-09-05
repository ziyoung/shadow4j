package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

@ChannelHandler.Sharable
public class ShadowStreamRawEncoder extends MessageToByteEncoder<ShadowStream> {

    public static final ShadowStreamRawEncoder INSTANCE = new ShadowStreamRawEncoder();

    @Override
    protected void encode(ChannelHandlerContext ctx, ShadowStream stream, ByteBuf byteBuf) throws Exception {
        byteBuf.writeBytes(stream.getData());
    }

}

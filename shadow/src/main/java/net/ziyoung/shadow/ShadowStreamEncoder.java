package net.ziyoung.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ShadowStreamEncoder extends MessageToByteEncoder<ShadowStream> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ShadowStream shadowStream, ByteBuf byteBuf) throws Exception {

    }

}

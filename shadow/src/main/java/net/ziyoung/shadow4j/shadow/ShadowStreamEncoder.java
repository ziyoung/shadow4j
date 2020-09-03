package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.security.SecureRandom;

public class ShadowStreamEncoder extends MessageToByteEncoder<ShadowStream> {

    protected static void encodeShadowStream(ShadowCipher cipher, ShadowStream shadowStream, ByteBuf byteBuf) throws Exception {
        byte[] data = shadowStream.getData();
        int size = data.length;
        byte[] lengthBytes = ShadowUtils.intToShortBytes(size);
        byteBuf.writeBytes(cipher.encrypt(lengthBytes));
        byteBuf.writeBytes(cipher.encrypt(data));
    }

    private final ShadowCipher cipher;

    public ShadowStreamEncoder(ShadowCipher cipher) {
        this.cipher = cipher;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ShadowStream shadowStream, ByteBuf byteBuf) throws Exception {
        ShadowUtils.checkShadowStream(shadowStream);
        encodeShadowStream(cipher, shadowStream, byteBuf);
    }

}

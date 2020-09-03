package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.security.SecureRandom;

public class ShadowAddressEncoder extends MessageToByteEncoder<ShadowAddress> {

    private final ShadowCipher cipher;
    private final byte[] salt;

    public ShadowAddressEncoder(ShadowConfig config) {
        this.cipher = new MetaCipher(config.getPassword(), config.getCipherName());
        this.salt = new byte[cipher.saltSize()];
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ShadowAddress shadowAddress, ByteBuf byteBuf) throws Exception {
        ShadowUtils.checkShadowStream(shadowAddress);

        new SecureRandom().nextBytes(salt);
        cipher.initEncrypt(salt);
        byteBuf.writeBytes(salt);
        ShadowStreamEncoder.encodeShadowStream(cipher, shadowAddress, byteBuf);

        ctx.pipeline().replace(this, null, new ShadowStreamEncoder(cipher));
        ctx.pipeline().addLast();
    }

}

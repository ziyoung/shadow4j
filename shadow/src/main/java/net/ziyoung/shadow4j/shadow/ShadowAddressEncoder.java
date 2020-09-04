package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.security.SecureRandom;

public class ShadowAddressEncoder extends MessageToByteEncoder<ShadowAddress> {

    private final ShadowCipher encryptCipher;
    private final ShadowCipher decryptCipher;
    private final byte[] salt;

    public ShadowAddressEncoder(ShadowConfig config) {
        this.encryptCipher = new MetaCipher(config.getPassword(), config.getCipherName());
        this.decryptCipher = new MetaCipher(config.getPassword(), config.getCipherName());
        this.salt = new byte[encryptCipher.saltSize()];
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ShadowAddress shadowAddress, ByteBuf byteBuf) throws Exception {
        ShadowUtils.checkShadowStream(shadowAddress);

        new SecureRandom().nextBytes(salt);
        encryptCipher.initEncrypt(salt);
        decryptCipher.initDecrypt(salt);

        byteBuf.writeBytes(salt);
        ShadowStreamEncoder.encodeShadowStream(encryptCipher, shadowAddress, byteBuf);

        ctx.pipeline().addAfter(ctx.name(), null, new ShadowStreamDecoder(decryptCipher));
        ctx.pipeline().addAfter(ctx.name(), null, new ShadowStreamEncoder(encryptCipher));
        ctx.pipeline().remove(this);
    }

}

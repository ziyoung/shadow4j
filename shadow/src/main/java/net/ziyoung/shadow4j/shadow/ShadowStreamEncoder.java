package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.security.SecureRandom;

public class ShadowStreamEncoder extends MessageToByteEncoder<ShadowStream> {

    private final ShadowCipher cipher;
    private boolean initCipherSalt = false;

    public ShadowStreamEncoder(ShadowConfig config) {
        cipher = new MetaCipher(config.getPassword(), config.getCipherName());
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ShadowStream shadowStream, ByteBuf byteBuf) throws Exception {
        ShadowUtils.checkShadowStream(shadowStream);
        if (!initCipherSalt) {
            if (!(shadowStream instanceof ShadowAddress)) {
                throw new EncoderException("salt is sent with ShadowAddress");
            }

            initCipherSalt = true;
            int size = cipher.saltSize();
            byte[] salt = new byte[size];
            new SecureRandom().nextBytes(salt);
            cipher.initEncrypt(salt);
            byteBuf.writeBytes(salt);
        }

        byte[] data = shadowStream.getData();
        int size = data.length;
        byte[] lengthBytes = ShadowUtils.intToShortBytes(size);
        byteBuf.writeBytes(cipher.encrypt(lengthBytes));
        byteBuf.writeBytes(cipher.encrypt(data));
    }

}

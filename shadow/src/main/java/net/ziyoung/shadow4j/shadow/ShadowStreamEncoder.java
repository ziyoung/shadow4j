package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

@Slf4j
public class ShadowStreamEncoder extends MessageToByteEncoder<ShadowStream> {

    private final ShadowCipher cipher;
    private final boolean isServerMode;
    private boolean initCipherSalt = false;

    public ShadowStreamEncoder(ShadowConfig config, boolean isServerMode) {
        this.cipher = new MetaCipher(config.getPassword(), config.getCipherName());
        this.isServerMode = isServerMode;
    }

    public ShadowStreamEncoder(ShadowConfig config) {
        this(config, false);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ShadowStream shadowStream, ByteBuf byteBuf) throws Exception {
        ShadowUtil.checkShadowStream(shadowStream);
        if (!initCipherSalt) {
            // for a shadow client, first stream packet should be ShadowAddress
            if (!isServerMode && !(shadowStream instanceof ShadowAddress)) {
                EncoderException exception = new EncoderException("for shadow client, salt is sent with ShadowAddress");
                log.error("encode error", exception);
                throw exception;
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
        byte[] lengthBytes = ShadowUtil.intToShortBytes(size);
        byteBuf.writeBytes(cipher.encrypt(lengthBytes));
        byteBuf.writeBytes(cipher.encrypt(data));
    }

}

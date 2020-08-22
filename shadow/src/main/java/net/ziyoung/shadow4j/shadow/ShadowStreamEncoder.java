package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.security.SecureRandom;

public class ShadowStreamEncoder extends MessageToByteEncoder<ShadowStream> {

    private final ShadowCipher cipher;
    private final byte[] salt;
    private boolean cipherInit = false;

    public ShadowStreamEncoder(ShadowConfig config) {
        this.cipher = new MetaCipher(config.getPassword(), config.getCipherName());
        this.salt = new byte[cipher.saltSize()];
    }

    private void init() throws Exception {
        new SecureRandom().nextBytes(salt);
        cipher.initEncrypt(salt);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ShadowStream shadowStream, ByteBuf byteBuf) throws Exception {
        if (!cipherInit) {
            init();
            cipherInit = true;
        }
        byte[] data = shadowStream.getData();
        int size = data == null ? 0 : data.length;
        if (size == 0 || size > ShadowStream.MAX_PAYLOAD_LENGTH) {
            throw new EncoderException("fail to decode stream: invalid data size " + size);
        }
        byteBuf.writeBytes(salt);
        byte[] lengthBytes = ShadowUtils.intToShortBytes(size);
        byteBuf.writeBytes(cipher.encrypt(lengthBytes));
        byteBuf.writeBytes(cipher.encrypt(data));
    }

}

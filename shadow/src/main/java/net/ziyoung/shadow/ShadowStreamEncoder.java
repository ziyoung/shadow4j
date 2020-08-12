package net.ziyoung.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class ShadowStreamEncoder extends MessageToByteEncoder<ShadowStream> {

    private final ShadowCipher cipher;
    private final byte[] salt;
    private boolean cipherInit = false;

    public ShadowStreamEncoder(ShadowConfig config) {
        byte[] psk = config.getCipherName().getBytes(StandardCharsets.UTF_8);
        this.cipher = new MetaCipher(psk, config.getCipherName());
        this.salt = new byte[cipher.saltSize()];
    }

    private void init() throws Exception {
        new SecureRandom().nextBytes(salt);
        cipher.initDecrypt(salt);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ShadowStream shadowStream, ByteBuf byteBuf) throws Exception {
        if (cipherInit) {
            init();
            cipherInit = true;
        }
        byte[] data = shadowStream.getData();
        int size = data == null ? 0 : data.length;
        if (size == 0 || size > ShadowStream.MAX_LENGTH) {
            throw new DecoderException("fail to decode: invalid data size " + size);
        }
        byteBuf.writeBytes(salt);
        byte[] lengthBytes = new byte[]{(byte) ((short) size >> 8), (byte) size};
        byteBuf.writeBytes(cipher.decrypt(lengthBytes));
        byteBuf.writeBytes(cipher.decrypt(data));
    }

}

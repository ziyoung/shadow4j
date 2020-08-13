package net.ziyoung.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.security.SecureRandom;

public class ShadowPacketEncoder extends MessageToByteEncoder<ShadowPacket> {

    private final ShadowCipher cipher;
    private final byte[] salt;
    private boolean cipherInit = false;

    public ShadowPacketEncoder(ShadowConfig config) {
        this.cipher = new MetaCipher(config.getPassword(), config.getCipherName(), false);
        this.salt = new byte[cipher.saltSize()];
    }

    private void init() throws Exception {
        new SecureRandom().nextBytes(salt);
        cipher.initEncrypt(salt);
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ShadowPacket shadowPacket, ByteBuf byteBuf) throws Exception {
        if (!cipherInit) {
            init();
            cipherInit = true;
        }
        byte[] data = shadowPacket.getData();
        int size = data == null ? 0 : data.length;
        if (size == 0 || size > ShadowPacket.MAX_PAYLOAD_LENGTH) {
            throw new EncoderException("fail to decode packet: invalid data size " + size);
        }
        byteBuf.writeBytes(salt);
        byteBuf.writeBytes(cipher.encrypt(data));
    }

}

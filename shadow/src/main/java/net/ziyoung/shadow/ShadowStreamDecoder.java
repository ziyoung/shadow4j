package net.ziyoung.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.StandardCharsets;
import java.util.List;

enum State {
    READ_SALT,
    READ_LENGTH,
    READ_PAYLOAD
}

public class ShadowStreamDecoder extends ReplayingDecoder<State> {

    private final ShadowCipher cipher;
    private int length;

    public ShadowStreamDecoder(ShadowConfig config) {
        byte[] psk = config.getCipherName().getBytes(StandardCharsets.UTF_8);
        this.cipher = new MetaCipher(psk, config.getCipherName());
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] bytes;
        byte[] plaintext;
        switch (state()) {
            case READ_SALT:
                byte[] salt = new byte[cipher.saltSize()];
                byteBuf.readBytes(salt);
                cipher.initDecrypt(salt);
                checkpoint(State.READ_LENGTH);
                break;
            case READ_LENGTH:
                bytes = new byte[MetaCipher.LENGTH_SIZE + MetaCipher.TAG_SIZE];
                byteBuf.readBytes(bytes);
                plaintext = cipher.decrypt(bytes);
                length = Integer.parseUnsignedInt(new String(plaintext));
                checkpoint(State.READ_PAYLOAD);
                break;
            case READ_PAYLOAD:
                bytes = new byte[length + MetaCipher.TAG_SIZE];
                byteBuf.readBytes(bytes);
                plaintext = cipher.decrypt(bytes);
                list.add(new ShadowStream(plaintext));
            default:
                throw new DecoderException("unreachable branch");
        }
    }

}

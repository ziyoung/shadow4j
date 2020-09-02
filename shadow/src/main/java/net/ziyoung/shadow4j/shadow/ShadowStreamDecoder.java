package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class ShadowStreamDecoder extends ReplayingDecoder<ShadowStreamDecoder.State> {

    private final ShadowCipher cipher;
    private int length;

    public ShadowStreamDecoder(ShadowConfig config) {
        super(State.READ_SALT);
        this.cipher = new MetaCipher(config.getPassword(), config.getCipherName());
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
                length = ShadowUtils.shorBytesToInt(plaintext) & ShadowStream.MAX_PAYLOAD_LENGTH;
                checkpoint(State.READ_PAYLOAD);
                break;
            case READ_PAYLOAD:
                bytes = new byte[length + MetaCipher.TAG_SIZE];
                byteBuf.readBytes(bytes);
                plaintext = cipher.decrypt(bytes);
                list.add(new ShadowStream(plaintext));
                checkpoint(State.READ_LENGTH);
                break;
            default:
                throw new DecoderException("unreachable branch");
        }
    }

    enum State {
        READ_SALT,
        READ_LENGTH,
        READ_PAYLOAD
    }

}

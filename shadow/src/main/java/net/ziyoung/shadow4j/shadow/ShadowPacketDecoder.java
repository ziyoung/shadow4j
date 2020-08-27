package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class ShadowPacketDecoder extends ReplayingDecoder<ShadowPacketDecoder.State> {

    private final ShadowCipher cipher;

    public ShadowPacketDecoder(ShadowConfig config) {
        super(State.READ_SALT);
        this.cipher = new MetaCipher(config.getPassword(), config.getCipherName(), false);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        switch (state()) {
            case READ_SALT:
                byte[] salt = new byte[cipher.saltSize()];
                byteBuf.readBytes(salt);
                cipher.initDecrypt(salt);
                checkpoint(State.READ_PAYLOAD);
                break;
            case READ_PAYLOAD:
                // we are using ReplayingDecoderByteBuf rather than ByteBuf, do not use byteBuf.readableBytes()
                byte[] bytes = new byte[byteBuf.writerIndex() - byteBuf.readerIndex()];
                byteBuf.readBytes(bytes);
                byte[] plaintext = cipher.decrypt(bytes);
                list.add(new ShadowPacket(plaintext));
                break;
            default:
                throw new DecoderException("unreachable branch");
        }
    }

    enum State {
        READ_SALT,
        READ_PAYLOAD
    }

}

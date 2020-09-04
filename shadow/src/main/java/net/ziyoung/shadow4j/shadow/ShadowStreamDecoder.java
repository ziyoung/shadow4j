package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
public class ShadowStreamDecoder extends ReplayingDecoder<ShadowStreamDecoder.State> {

    private final ShadowCipher cipher;
    private int length;

    public ShadowStreamDecoder(ShadowCipher cipher) {
        super(State.READ_LENGTH);
        this.cipher = cipher;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] bytes;
        byte[] plaintext;
        try {
            switch (this.state()) {
                case READ_LENGTH:
                    bytes = new byte[MetaCipher.LENGTH_SIZE + MetaCipher.TAG_SIZE];
                    byteBuf.readBytes(bytes);
                    log.debug("length bytes is {}", bytes);
                    plaintext = cipher.decrypt(bytes);
                    length = ShadowUtils.shorBytesToInt(plaintext) & ShadowStream.MAX_PAYLOAD_LENGTH;
                    log.debug("length is {}", length);
                    this.checkpoint(State.READ_PAYLOAD);
                    break;
                case READ_PAYLOAD:
                    bytes = new byte[length + MetaCipher.TAG_SIZE];
                    byteBuf.readBytes(bytes);
                    plaintext = cipher.decrypt(bytes);
                    list.add(new ShadowStream(plaintext));
                    this.checkpoint(State.READ_LENGTH);
                    break;
                case FAILURE:
                    byteBuf.skipBytes(this.actualReadableBytes());
                    break;
            }
        } catch (Exception cause) {
            fail(ctx, list, cause);
        }
    }

    private void fail(ChannelHandlerContext ctx, List<Object> list, Exception cause) {
        log.error("decode shadow stream error: ", cause);
        this.checkpoint(State.FAILURE);

        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(cause.toString().getBytes(StandardCharsets.UTF_8));
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                byteBuf);
        list.add(httpResponse);
    }

    enum State {
        READ_LENGTH,
        READ_PAYLOAD,
        FAILURE
    }

}

package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
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
    private final boolean isServerMode;
    private boolean hasDecodeAddress;
    private int length;

    public ShadowStreamDecoder(ShadowConfig config, boolean isServerMode) {
        super(State.READ_SALT);
        this.cipher = new MetaCipher(config.getPassword(), config.getCipherName());
        this.isServerMode = isServerMode;
    }

    public ShadowStreamDecoder(ShadowConfig config) {
        this(config, false);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] bytes;
        byte[] plaintext;
        try {
            switch (this.state()) {
                case READ_SALT:
                    int size = cipher.saltSize();
                    byte[] salt = new byte[size];
                    byteBuf.readBytes(salt);
                    cipher.initDecrypt(salt);
                    this.checkpoint(State.READ_LENGTH);
                case READ_LENGTH:
                    bytes = new byte[MetaCipher.LENGTH_SIZE + MetaCipher.TAG_SIZE];
                    byteBuf.readBytes(bytes);
                    plaintext = cipher.decrypt(bytes);
                    length = ShadowUtil.shorBytesToInt(plaintext) & ShadowStream.MAX_PAYLOAD_LENGTH;
                    this.checkpoint(State.READ_PAYLOAD);
                case READ_PAYLOAD:
                    bytes = new byte[length + MetaCipher.TAG_SIZE];
                    byteBuf.readBytes(bytes);
                    plaintext = cipher.decrypt(bytes);

                    ShadowStream stream;
                    if (isServerMode && !hasDecodeAddress) {
                        stream = new ShadowAddress(plaintext);
                        hasDecodeAddress = true;
                    } else {
                        stream = new ShadowStream(plaintext);
                    }
                    list.add(stream);

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
        // FIXME: HttpResponseEncoder conflicts with ShadowStreamRawEncoder
        FullHttpResponse httpResponse = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,
                HttpResponseStatus.INTERNAL_SERVER_ERROR,
                byteBuf);
        list.add(httpResponse);
    }

    enum State {
        READ_SALT,
        READ_LENGTH,
        READ_PAYLOAD,
        FAILURE
    }

}

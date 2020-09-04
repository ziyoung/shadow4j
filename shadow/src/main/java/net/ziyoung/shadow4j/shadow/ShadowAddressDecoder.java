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
public class ShadowAddressDecoder extends ReplayingDecoder<ShadowAddressDecoder.State> {

    private final ShadowCipher decryptCipher;
    private final ShadowCipher encryptCipher;
    private int length;

    public ShadowAddressDecoder(ShadowConfig config) {
        super(ShadowAddressDecoder.State.READ_SALT);
        this.decryptCipher = new MetaCipher(config.getPassword(), config.getCipherName());
        this.encryptCipher = new MetaCipher(config.getPassword(), config.getCipherName());
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws Exception {
        byte[] bytes;
        byte[] plaintext;
        try {
            switch (this.state()) {
                case READ_SALT:
                    int size = decryptCipher.saltSize();
                    byte[] salt = new byte[size];
                    byteBuf.readBytes(salt);
                    decryptCipher.initDecrypt(salt);
                    encryptCipher.initEncrypt(salt);
                    this.checkpoint(State.READ_LENGTH);
                    break;
                case READ_LENGTH:
                    bytes = new byte[MetaCipher.LENGTH_SIZE + MetaCipher.TAG_SIZE];
                    byteBuf.readBytes(bytes);
                    plaintext = decryptCipher.decrypt(bytes);
                    length = ShadowUtils.shorBytesToInt(plaintext) & ShadowStream.MAX_PAYLOAD_LENGTH;
                    this.checkpoint(State.READ_PAYLOAD);
                    break;
                case READ_PAYLOAD:
                    bytes = new byte[length + MetaCipher.TAG_SIZE];
                    byteBuf.readBytes(bytes);
                    plaintext = decryptCipher.decrypt(bytes);
                    list.add(new ShadowAddress(plaintext));

                    ctx.pipeline().addAfter(ctx.name(), null, new ShadowStreamDecoder(decryptCipher));
                    ctx.pipeline().addAfter(ctx.name(), null, new ShadowStreamEncoder(encryptCipher));
                    ctx.pipeline().remove(this);
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
        log.error("decode shadow address error: ", cause);
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
        READ_SALT,
        READ_LENGTH,
        READ_PAYLOAD,
        FAILURE
    }

}

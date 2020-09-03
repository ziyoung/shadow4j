package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class ShadowStreamDecoderTest {

    private static final byte[] PASSWORD = "change this password to a secret".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PLAINTEXT = "example plaintext".getBytes(StandardCharsets.UTF_8);
    private static final String SERVER = "example:3000";
    private static final ShadowConfig AES_CONFIG = new ShadowConfig(null, "aes", PASSWORD);
    private static final ShadowConfig CHACHA_20_CONFIG = new ShadowConfig(null, "chacha20", PASSWORD);

    @Test
    @DisplayName("test decoding shadow stream")
    void testStreamDecoded() {
        ShadowConfig[] configs = new ShadowConfig[]{AES_CONFIG, CHACHA_20_CONFIG};
        for (ShadowConfig config : configs) {
            EmbeddedChannel channel = new EmbeddedChannel(new ShadowAddressDecoder(config));

            ByteBuf[] byteBufTuple = Assertions.assertDoesNotThrow(() -> byteBufTuple(config));
            Assertions.assertEquals(2, byteBufTuple.length);
            Assertions.assertTrue(channel.writeInbound((Object[]) byteBufTuple));
            Assertions.assertTrue(channel.finish());

            ShadowAddress address = channel.readInbound();
            Assertions.assertNotNull(address.getData());
            Assertions.assertEquals(SERVER, address.toString());

            ShadowStream stream = channel.readInbound();
            Assertions.assertNotNull(address.getData());
            Assertions.assertArrayEquals(PLAINTEXT, stream.getData());
        }
    }

    private ByteBuf[] byteBufTuple(ShadowConfig config) throws Exception {
        ShadowCipher cipher = new MetaCipher(config.getPassword(), config.getCipherName());
        ShadowAddress address = ShadowAddress.valueOf(new URI("ss://" + SERVER));
        ShadowStream stream = new ShadowStream(PLAINTEXT);

        ByteBuf addressByteBuf = Unpooled.buffer();
        ByteBuf streamByteBuf = Unpooled.buffer();

        byte[] salt = new byte[cipher.saltSize()];
        new SecureRandom().nextBytes(salt);
        cipher.initEncrypt(salt);
        addressByteBuf.writeBytes(salt);

        // encrypt address
        int size = address.getData().length;
        byte[] lengthBytes = ShadowUtils.intToShortBytes(size);
        addressByteBuf.writeBytes(cipher.encrypt(lengthBytes));
        addressByteBuf.writeBytes(cipher.encrypt(address.getData()));

        // encrypt stream
        size = stream.getData().length;
        lengthBytes = ShadowUtils.intToShortBytes(size);
        streamByteBuf.writeBytes(cipher.encrypt(lengthBytes));
        streamByteBuf.writeBytes(cipher.encrypt(stream.getData()));

        return new ByteBuf[]{addressByteBuf, streamByteBuf};
    }

}

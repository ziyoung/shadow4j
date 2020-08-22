package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class ShadowPacketDecoderTest {

    private static final byte[] password = "change this password to a secret".getBytes(StandardCharsets.UTF_8);
    private static final byte[] plaintext = "example plaintext".getBytes(StandardCharsets.UTF_8);
    private static final ShadowConfig aesConfig = new ShadowConfig("aes", password);
    private static final ShadowConfig chacha20Config = new ShadowConfig("chacha20", password);

    @Test
    @DisplayName("test decoding shadow packet")
    void testPacketDecoded() {
        ShadowConfig[] configs = new ShadowConfig[]{aesConfig, chacha20Config};
        for (ShadowConfig config : configs) {
            EmbeddedChannel channel = new EmbeddedChannel(new ShadowPacketDecoder(config));
            ByteBuf byteBuf = Assertions.assertDoesNotThrow(() -> prepareByteBuf(config));
            Assertions.assertTrue(channel.writeInbound(byteBuf));
            Assertions.assertTrue(channel.finish());
            ShadowPacket shadowPacket = channel.readInbound();
            Assertions.assertNotNull(shadowPacket.getData());
            Assertions.assertArrayEquals(plaintext, shadowPacket.getData());
        }
    }

    private ByteBuf prepareByteBuf(ShadowConfig config) throws Exception {
        ShadowCipher cipher = new MetaCipher(config.getPassword(), config.getCipherName(), false);
        byte[] salt = new byte[cipher.saltSize()];
        new SecureRandom().nextBytes(salt);
        cipher.initEncrypt(salt);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(salt);
        byteBuf.writeBytes(cipher.encrypt(plaintext));
        return byteBuf;
    }


}

package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class ShadowPacketEncoderTest {

    private static final byte[] password = "change this password to a secret".getBytes(StandardCharsets.UTF_8);
    private static final byte[] plaintext = "example plaintext".getBytes(StandardCharsets.UTF_8);
    private static final ShadowConfig aesConfig = new ShadowConfig("aes", password);
    private static final ShadowConfig chacha20Config = new ShadowConfig("chacha20", password);


    @Test
    @DisplayName("test encoding shadow packet")
    void testPacketEncoded() {
        ShadowConfig[] configs = new ShadowConfig[]{aesConfig, chacha20Config};
        ShadowPacket shadowPacket = new ShadowPacket(plaintext);
        for (ShadowConfig config : configs) {
            EmbeddedChannel channel = new EmbeddedChannel(new ShadowPacketEncoder(config));
            Assertions.assertTrue(channel.writeOutbound(shadowPacket));
            Assertions.assertTrue(channel.finish());

            ByteBuf byteBuf = channel.readOutbound();
            ShadowCipher cipher = new MetaCipher(config.getPassword(), config.getCipherName(), false);

            {
                int size = cipher.saltSize();
                byte[] bytes = new byte[size];
                byteBuf.readBytes(bytes);
                Assertions.assertDoesNotThrow(() -> cipher.initEncrypt(bytes));
            }

            {
                int size = plaintext.length + MetaCipher.TAG_SIZE;
                byte[] bytes = new byte[size];
                byteBuf.readBytes(bytes);
                byte[] ciphertext = Assertions.assertDoesNotThrow(() -> cipher.encrypt(plaintext));
                Assertions.assertArrayEquals(ciphertext, bytes);
            }

            Assertions.assertEquals(0, byteBuf.readableBytes());
            byteBuf.release();
            Assertions.assertNull(channel.readOutbound());
        }
    }
}

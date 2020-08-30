package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class ShadowStreamEncoderTest {

    private static final byte[] PASSWORD = "change this password to a secret".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PLAINTEXT = "example plaintext".getBytes(StandardCharsets.UTF_8);
    private static final ShadowConfig AES_CONFIG = new ShadowConfig(null, "aes", PASSWORD);
    private static final ShadowConfig CHACHA_20_CONFIG = new ShadowConfig(null, "chacha20", PASSWORD);

    @Test
    @DisplayName("test encoding shadow stream")
    void testStreamEncoded() {
        ShadowStream shadowStream = new ShadowStream(PLAINTEXT);
        ShadowConfig[] configs = new ShadowConfig[]{AES_CONFIG, CHACHA_20_CONFIG};
        for (ShadowConfig config : configs) {
            EmbeddedChannel channel = new EmbeddedChannel(new ShadowStreamEncoder(config));
            Assertions.assertTrue(channel.writeOutbound(shadowStream));
            Assertions.assertTrue(channel.finish());

            ByteBuf byteBuf = channel.readOutbound();
            ShadowCipher cipher = new MetaCipher(config.getPassword(), config.getCipherName());
            {
                int size = cipher.saltSize();
                byte[] bytes = new byte[size];
                byteBuf.readBytes(bytes);
                Assertions.assertDoesNotThrow(() -> cipher.initEncrypt(bytes));
            }

            {
                int size = 2 + MetaCipher.TAG_SIZE;
                byte[] bytes = new byte[size];
                byteBuf.readBytes(bytes);
                int length = PLAINTEXT.length;
                byte[] lengthBytes = ShadowUtils.intToShortBytes(length);
                byte[] ciphertext = Assertions.assertDoesNotThrow(() -> cipher.encrypt(lengthBytes));
                Assertions.assertArrayEquals(ciphertext, bytes);
            }

            {
                int size = PLAINTEXT.length + MetaCipher.TAG_SIZE;
                byte[] bytes = new byte[size];
                byteBuf.readBytes(bytes);
                byte[] ciphertext = Assertions.assertDoesNotThrow(() -> cipher.encrypt(PLAINTEXT));
                Assertions.assertArrayEquals(ciphertext, bytes);
            }

            Assertions.assertEquals(0, byteBuf.readableBytes());
            byteBuf.release();
            Assertions.assertNull(channel.readOutbound());
        }
    }

//    @Test
//    @DisplayName("use cipher to decode address")
//    void testEncodeAddress() {
//        byte[] password = "123456".getBytes(StandardCharsets.UTF_8);
//        byte[] key = Assertions.assertDoesNotThrow(() -> KdUtil.computeKdf(password, 32));
//        ShadowConfig config = new ShadowConfig(null, "chacha20-ietf-poly1305", key);
//        ShadowCipher cipher = new MetaCipher(config.getPassword(), config.getCipherName());
//        byte[] salt = new byte[32];
//        log.warn("salt {}", salt);
//        Assertions.assertDoesNotThrow(() -> cipher.initEncrypt(salt));
//        SocksAddress address = Assertions.assertDoesNotThrow(() -> SocksAddress.valueOf("baidu.com", 80));
//        String result = "0953f710e165bf1b860e810371afc33eed9d2914fd401762b9d50243a9e5b5bcb1a06e3363334b66ef5811b04cc542d220cf139548dcfb6296e9a40f45fbe46db2488daa65f9ffbb4d1fcc1617c144f049d5086f6b75b1fb55f37c3ab8e27e0fed2c24580b";
//        byte[] bytes = Assertions.assertDoesNotThrow(() -> cipher.encrypt(address.getData()));
//        log.warn("bytes {}", bytes);
//        Assertions.assertEquals(result, Hex.encodeHexString(bytes));
//    }

}

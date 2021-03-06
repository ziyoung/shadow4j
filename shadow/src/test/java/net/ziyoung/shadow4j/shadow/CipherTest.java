package net.ziyoung.shadow4j.shadow;

import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class CipherTest {

    private static final byte[] PASSWORD = "change this password to a secret".getBytes(StandardCharsets.UTF_8);
    private static final byte[] PLAINTEXT = "example plaintext".getBytes(StandardCharsets.UTF_8);
    private static final byte[] SALT = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    @Test
    @DisplayName("test ase-gcm")
    void testAesGcm() {
        // see https://play.golang.org/p/YV3yiPzapMu
        String[] results = new String[]{
                "8f9eb4765c7e65eebec0bedd50b351196a126b21330e611a084acc42432c31f1c7",
                "7e920a58188fb7b1f00d69417f3d673ac1e318aaf645aa6c57fc9f0b16b3eee87b",
                "b51163353a697c2d00122a749ac82bc152f0f44336df52697faf8cfdfa8a5a1cfe"
        };
        int[] sizes = new int[]{16, 24, 32};
        for (int i = 0; i < sizes.length; i++) {
            int size = sizes[i];
            String result = results[i];
            byte[] pwd = Arrays.copyOf(PASSWORD, size);
            ShadowCipher cipher = new MetaCipher(pwd, "aes");
            encryptAndDecrypt(cipher, result);
        }
    }

    @Test
    @DisplayName("test chacha20-poly1035")
    void testChacha20() {
        // https://play.golang.org/p/u9ORp0dSo8y
        String result = "5310756ef93fbd0641102f25f6ab83d59587441a2439b4aa4eef475fe8f6512277";
        ShadowCipher cipher = new MetaCipher(PASSWORD, "chacha20");
        encryptAndDecrypt(cipher, result);
    }

    @Test
    @DisplayName("test kdf")
    void testKdf() {
        // see https://play.golang.org/p/jSirI1lXWiW
        String result = "26091960993f19de456d340a7d0482a892e91a230364e1b85a8fb6a2e7666f5b";
        int size = 32;
        byte[] key = Assertions.assertDoesNotThrow(() -> KdUtil.computeKdf(PASSWORD, 32));
        Assertions.assertEquals(size, key.length);
        Assertions.assertEquals(result, Hex.encodeHexString(key));
    }

    @Test
    @DisplayName("test hkdf sha1")
    void testHkdfSha1() {
        // https://play.golang.org/p/fxVIOlEs_an
        String result = "65a1621f4f54730f90bc5338f108b5d910279319b1abbf190d162483834401c7";
        byte[] info = "ss-subkey".getBytes(StandardCharsets.UTF_8);
        byte[] key = Assertions.assertDoesNotThrow(() -> KdUtil.hkdfSha1(PASSWORD, SALT, info, PASSWORD.length));
        Assertions.assertEquals(PASSWORD.length, key.length);
        Assertions.assertEquals(result, Hex.encodeHexString(key));
    }

    private void encryptAndDecrypt(ShadowCipher cipher, String result) {
        Assertions.assertDoesNotThrow(() -> cipher.initEncrypt(SALT));
        byte[] ciphertext = Assertions.assertDoesNotThrow(() -> cipher.encrypt(PLAINTEXT));
        Assertions.assertEquals(result, Hex.encodeHexString(ciphertext));

        Assertions.assertDoesNotThrow(() -> cipher.initDecrypt(SALT));
        byte[] decrypttext = Assertions.assertDoesNotThrow(() -> cipher.decrypt(ciphertext));
        Assertions.assertArrayEquals(PLAINTEXT, decrypttext);
    }

}

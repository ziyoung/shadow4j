package net.ziyoung.shadow;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class CipherTests {

    private static final byte[] password = "change this password to a secret".getBytes(StandardCharsets.UTF_8);
    private static final byte[] plaintext = "example plaintext".getBytes(StandardCharsets.UTF_8);
    private static final byte[] nonce = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] salt = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

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
            byte[] pwd = Arrays.copyOf(password, size);
            ShadowCipher cipher = new MetaCipher(pwd, "aes");
            byte[] ciphertext = Assertions.assertDoesNotThrow(() -> cipher.encrypt(salt, nonce, plaintext));
            Assertions.assertEquals(result, Hex.encodeHexString(ciphertext));
            byte[] decrypttext = Assertions.assertDoesNotThrow(() -> cipher.decrypt(salt, nonce, ciphertext));
            Assertions.assertArrayEquals(plaintext, decrypttext);
        }
    }

    @Test
    @DisplayName("test kdf")
    void testKdf() {
        // see https://play.golang.org/p/jSirI1lXWiW
        String hexString = "26091960993f19de456d340a7d0482a892e91a230364e1b85a8fb6a2e7666f5b";
        int size = 32;
        byte[] key = Assertions.assertDoesNotThrow(() -> KdUtil.computeKdf(password, 32));
        Assertions.assertEquals(size, key.length);
        Assertions.assertEquals(hexString, Hex.encodeHexString(key));
    }

}

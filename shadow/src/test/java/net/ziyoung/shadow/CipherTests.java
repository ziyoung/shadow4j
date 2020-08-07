package net.ziyoung.shadow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

public class CipherTests {

    private static final byte[] password = "change this password to a secret".getBytes(StandardCharsets.UTF_8);
    private static final byte[] plaintext = "example plaintext".getBytes(StandardCharsets.UTF_8);
    private static final byte[] nonce = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};

    @Test
    @DisplayName("test ase-gcm")
    void testAesGcm() {
        int[] sizes = new int[]{16, 24, 32};
        for (int size : sizes) {
            byte[] pwd = Arrays.copyOf(password, size);
            ShadowCipher cipher = new MetaCipher(pwd, "aes");
            byte[] salt = genSalt(cipher);
            byte[] ciphertext = Assertions.assertDoesNotThrow(() -> cipher.encrypt(salt, nonce, plaintext));
            byte[] decrypttext = Assertions.assertDoesNotThrow(() -> cipher.decrypt(salt, nonce, ciphertext));
            Assertions.assertArrayEquals(plaintext, decrypttext);
        }
    }

    @Test
    @DisplayName("test chacha20")
    void testChacha20() {
        ShadowCipher cipher = new MetaCipher(password, "chacha20");
        byte[] salt = genSalt(cipher);
        byte[] ciphertext = Assertions.assertDoesNotThrow(() -> cipher.encrypt(salt, nonce, plaintext));
        byte[] decrypttext = Assertions.assertDoesNotThrow(() -> cipher.decrypt(salt, nonce, ciphertext));
        Assertions.assertArrayEquals(plaintext, decrypttext);
    }

    private byte[] genSalt(ShadowCipher cipher) {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[cipher.saltSize()];
        random.nextBytes(salt);
        return salt;
    }

}

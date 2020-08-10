package net.ziyoung.shadow;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

/***
 * cipher names: https://docs.oracle.com/en/java/javase/14/docs/specs/security/standard-names.html
 */

public class MetaCipher implements ShadowCipher {

    private static final byte[] hkdfInfo = "ss-subkey".getBytes(StandardCharsets.UTF_8);

    private final byte[] psk;
    private final String cipherName;

    public MetaCipher(byte[] psk, String cipherName) {
        if (!("aes".equals(cipherName) || "chacha20".equals(cipherName))) {
            throw new IllegalArgumentException("invalid cipher name");
        }
        this.psk = psk;
        this.cipherName = cipherName;
        this.init();
    }

    private void init() {
        if (isAes()) {
            switch (keySize()) {
                case 16: // aes-128-gcm
                case 24: // aes-192-gcm
                case 32: // aes-256-gcm
                    break;
                default:
                    throw new IllegalArgumentException("invalid aes key");
            }
        } else {
            if (keySize() != 32) {
                throw new IllegalArgumentException("invalid chacha20 key");
            }
        }
    }

    private Key subKey(byte[] salt) throws Exception {
        byte[] key = KdUtil.hkdfSha1(psk, salt, hkdfInfo, keySize());
        String algorithm = isAes() ? "AES" : "ChaCha20";
        return new SecretKeySpec(key, algorithm);
    }

    private AlgorithmParameterSpec parameterSpec(byte[] iv) {
        return isAes() ? new GCMParameterSpec(128, iv) : new IvParameterSpec(iv);
    }

    private Cipher cipherInstance(int mode, Key key, AlgorithmParameterSpec parameters) throws Exception {
        String transformation = isAes() ? "AES/GCM/NoPadding" : "ChaCha20-Poly1305/None/NoPadding";
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(mode, key, parameters);
        return cipher;
    }

    private boolean isAes() {
        return "aes".equals(cipherName);
    }

    @Override
    public int keySize() {
        return psk.length;
    }

    @Override
    public int saltSize() {
        return Math.max(keySize(), 16);
    }

    @Override
    public byte[] encrypt(byte[] salt, byte[] nonce, byte[] plaintext) throws Exception {
        Key key = subKey(salt);
        Cipher cipher = cipherInstance(Cipher.ENCRYPT_MODE, key, parameterSpec(nonce));
        return cipher.doFinal(plaintext);
    }

    @Override
    public byte[] decrypt(byte[] salt, byte[] nonce, byte[] ciphertext) throws Exception {
        Key key = subKey(salt);
        Cipher cipher = cipherInstance(Cipher.DECRYPT_MODE, key, parameterSpec(nonce));
        return cipher.doFinal(ciphertext);
    }

}

package net.ziyoung.shadow;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

/***
 * cipher names: https://docs.oracle.com/en/java/javase/14/docs/specs/security/standard-names.html
 */

@Slf4j
public class MetaCipher implements ShadowCipher {

    private final byte[] psk;
    private final String cipherName;
    private Key key;

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
            key = new SecretKeySpec(psk, "AES");
        } else {
            if (keySize() != 32) {
                throw new IllegalArgumentException("invalid chacha20 key");
            }
            key = new SecretKeySpec(psk, "ChaCha20");
        }
    }

    private AlgorithmParameterSpec ivSpec(byte[] iv) {
        return isAes() ? new GCMParameterSpec(128, iv) : new IvParameterSpec(iv);
    }

    private Cipher cipherInstance(int mode, AlgorithmParameterSpec parameterSpec) throws Exception {
        String transformation = isAes() ? "AES/GCM/NoPadding" : "ChaCha20-Poly1305/None/NoPadding";
        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(mode, key, parameterSpec);
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
    public byte[] encrypt(byte[] plaintext, byte[] iv) throws Exception {
        Cipher cipher = cipherInstance(Cipher.ENCRYPT_MODE, ivSpec(iv));
        return cipher.doFinal(plaintext);
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] iv) throws Exception {
        Cipher cipher = cipherInstance(Cipher.DECRYPT_MODE, ivSpec(iv));
        return cipher.doFinal(ciphertext);
    }

}

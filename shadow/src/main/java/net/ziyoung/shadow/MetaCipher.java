package net.ziyoung.shadow;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

/***
 * cipher names: https://docs.oracle.com/en/java/javase/14/docs/specs/security/standard-names.html
 */
public class MetaCipher implements ShadowCipher {

    public static final int TAG_SIZE = 16;
    private static final byte[] HKDF_INFO = "ss-subkey".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DEFAULT_NONCE = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private final byte[] psk;
    private final String cipherName;
    private byte[] nonce;
    private int mode = -1;
    private Cipher curCipher;

    public MetaCipher(byte[] psk, String cipherName) {
        if (!("aes".equals(cipherName) || "chacha20".equals(cipherName))) {
            throw new IllegalArgumentException("invalid cipher name");
        }
        this.psk = psk;
        this.cipherName = cipherName;
        this.nonce = Arrays.copyOf(DEFAULT_NONCE, DEFAULT_NONCE.length);
        this.validateKey();
    }

    private void validateKey() {
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
        byte[] key = KdUtil.hkdfSha1(psk, salt, HKDF_INFO, keySize());
        String algorithm = isAes() ? "AES" : "ChaCha20";
        return new SecretKeySpec(key, algorithm);
    }

    private AlgorithmParameterSpec parameterSpec(byte[] iv) {
        return isAes() ? new GCMParameterSpec(TAG_SIZE * 8, iv) : new IvParameterSpec(iv);
    }

    private Cipher cipherInstance(int mode, Key key, AlgorithmParameterSpec parameters) throws Exception {
        if (curCipher == null) {
            String transformation = isAes() ? "AES/GCM/NoPadding" : "ChaCha20-Poly1305/None/NoPadding";
            curCipher = Cipher.getInstance(transformation);
        }
        curCipher.init(mode, key, parameters);
        return curCipher;
    }

    private boolean isAes() {
        return "aes".equals(cipherName);
    }

    public void increaseNonce() {
        for (int i = 0; i < nonce.length; i++) {
            nonce[i]++;
            if (nonce[i] != 0) {
                return;
            }
        }
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
    public void initEncrypt(byte[] salt) throws Exception {
        mode = Cipher.ENCRYPT_MODE;
        nonce = Arrays.copyOf(DEFAULT_NONCE, DEFAULT_NONCE.length);
        curCipher = cipherInstance(mode, subKey(salt), parameterSpec(nonce));
    }

    @Override
    public byte[] encrypt(byte[] plaintext) throws Exception {
        if (mode != Cipher.ENCRYPT_MODE) {
            throw new IllegalStateException("invalid mode " + mode);
        }
        increaseNonce();
        return curCipher.doFinal(plaintext);
    }

    @Override
    public void initDecrypt(byte[] salt) throws Exception {
        mode = Cipher.DECRYPT_MODE;
        nonce = Arrays.copyOf(DEFAULT_NONCE, DEFAULT_NONCE.length);
        curCipher = cipherInstance(mode, subKey(salt), parameterSpec(nonce));
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) throws Exception {
        if (mode != Cipher.DECRYPT_MODE) {
            throw new IllegalStateException("invalid mode " + mode);
        }
        increaseNonce();
        return curCipher.doFinal(ciphertext);
    }

}

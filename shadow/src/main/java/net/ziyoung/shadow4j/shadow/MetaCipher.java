package net.ziyoung.shadow4j.shadow;

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
    public static final int LENGTH_SIZE = 2;
    private static final byte[] HKDF_INFO = "ss-subkey".getBytes(StandardCharsets.UTF_8);
    private static final byte[] DEFAULT_NONCE = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    private final byte[] psk;
    private final String cipherName;
    private final byte[] nonce;
    private boolean increaseNonce = true;

    private byte[] salt;
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

    public MetaCipher(byte[] psk, String cipherName, boolean increaseNonce) {
        this(psk, cipherName);
        this.increaseNonce = false;
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

    private Cipher cipherInstance(AlgorithmParameterSpec parameters) throws Exception {
        // for chacha20-poly1305, can't use previous cipher
        if (curCipher == null || !isAes()) {
            String transformation = isAes() ? "AES/GCM/NoPadding" : "ChaCha20-Poly1305/None/NoPadding";
            curCipher = Cipher.getInstance(transformation);
        }
        curCipher.init(mode, subKey(salt), parameters);
        return curCipher;
    }

    private boolean isAes() {
        return "aes".equals(cipherName);
    }

    public void updateNonceAndCipher() throws Exception {
        for (int i = 0; i < nonce.length; i++) {
            nonce[i]++;
            if (nonce[i] != 0) {
                break;
            }
        }
        curCipher = cipherInstance(parameterSpec(nonce));
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
        if (mode != Cipher.ENCRYPT_MODE) {
            mode = Cipher.ENCRYPT_MODE;
            System.arraycopy(DEFAULT_NONCE, 0, nonce, 0, nonce.length);
        }
        this.salt = salt;
        curCipher = cipherInstance(parameterSpec(nonce));
    }

    @Override
    public byte[] encrypt(byte[] plaintext) throws Exception {
        if (mode != Cipher.ENCRYPT_MODE) {
            throw new IllegalStateException("invalid mode " + mode);
        }
        byte[] bytes = curCipher.doFinal(plaintext);
        if (increaseNonce) {
            updateNonceAndCipher();
        }
        return bytes;
    }

    @Override
    public void initDecrypt(byte[] salt) throws Exception {
        if (mode != Cipher.DECRYPT_MODE) {
            mode = Cipher.DECRYPT_MODE;
            System.arraycopy(DEFAULT_NONCE, 0, nonce, 0, nonce.length);
        }
        this.salt = salt;
        curCipher = cipherInstance(parameterSpec(nonce));
    }

    @Override
    public byte[] decrypt(byte[] ciphertext) throws Exception {
        if (mode != Cipher.DECRYPT_MODE) {
            throw new IllegalStateException("invalid mode " + mode);
        }
        byte[] bytes = curCipher.doFinal(ciphertext);
        if (increaseNonce) {
            updateNonceAndCipher();
        }
        return bytes;
    }

}

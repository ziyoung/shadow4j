package net.ziyoung.shadow;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

@Slf4j
public class MetaCipher implements ShadowCipher {

    private final byte[] psk;
    private final byte[] iv;
    private Key key;
    private Cipher cipher;

    public MetaCipher(byte[] psk, byte[] iv, String cipherName) throws Exception {
        if (!("aes".equals(cipherName) || "chacha".equals(cipherName))) {
            throw new IllegalArgumentException("invalid cipher name");
        }
        this.psk = psk;
        this.iv = iv;
        this.init(cipherName);
    }

    private void init(String cipherName) throws Exception {
        if ("aes".equals(cipherName)) {
            switch (keySize()) {
                case 16: // aes-128-gcm
                case 24: // aes-192-gcm
                case 32: // aes-256-gcm
                    break;
                default:
                    throw new IllegalArgumentException("invalid key");
            }
            key = new SecretKeySpec(psk, "AES");
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
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
    public byte[] encrypt(byte[] plaintext, byte[] iv) throws Exception {
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);
        return cipher.doFinal(plaintext);
    }

    @Override
    public byte[] decrypt(byte[] ciphertext, byte[] iv) throws Exception {
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);
        return cipher.doFinal(ciphertext);
    }

}

package net.ziyoung.shadow;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplate;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AesGcmKeyManager;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.security.GeneralSecurityException;

@Slf4j
@AllArgsConstructor
public class MetaCipher implements ShadowCipher {

    static {
        try {
            AeadConfig.register();
        } catch (GeneralSecurityException e) {
            log.error("register error", e);
            throw new RuntimeException("AeadConfig register failure");
        }
    }

//    private static byte[] hkdfSha1(byte[] secret,byte[] salt, byte[] info) {
//
//    }

    private final byte[] psk;

    @Override
    public int keySize() {
        return psk.length;
    }

    @Override
    public int saltSize() {
        return Math.max(keySize(), 16);
    }

    @Override
    public Aead encrypter(byte[] salt) throws GeneralSecurityException {
        KeyTemplate keyTemplate = AesGcmKeyManager.aes128GcmTemplate();
        KeysetHandle handle = KeysetHandle.generateNew(keyTemplate);
        return handle.getPrimitive(Aead.class);
    }

    @Override
    public Aead decrypter(byte[] salt) {
        return null;
    }

}

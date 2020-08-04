package net.ziyoung.shadow;

import com.google.crypto.tink.Aead;

import java.security.GeneralSecurityException;

public interface ShadowCipher {

    int keySize();

    int saltSize();

    Aead encrypter(byte[] salt) throws GeneralSecurityException;

    Aead decrypter(byte[] salt);

}

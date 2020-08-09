package net.ziyoung.shadow;

public interface ShadowCipher {

    int keySize();

    int saltSize();

    byte[] encrypt(byte[] salt, byte[] nonce, byte[] plaintext) throws Exception;

    byte[] decrypt(byte[] salt, byte[] nonce, byte[] ciphertext) throws Exception;

}

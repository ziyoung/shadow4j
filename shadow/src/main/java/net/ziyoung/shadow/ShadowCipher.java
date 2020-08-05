package net.ziyoung.shadow;

public interface ShadowCipher {

    int keySize();

    int saltSize();

    byte[] encrypt(byte[] plaintext, byte[] iv) throws Exception;

    byte[] decrypt(byte[] ciphertext, byte[] iv) throws Exception;

}

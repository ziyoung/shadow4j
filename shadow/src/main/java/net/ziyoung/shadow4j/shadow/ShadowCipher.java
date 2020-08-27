package net.ziyoung.shadow4j.shadow;

public interface ShadowCipher {

    int keySize();

    int saltSize();

    void initEncrypt(byte[] salt) throws Exception;

    byte[] encrypt(byte[] plaintext) throws Exception;

    void initDecrypt(byte[] salt) throws Exception;

    byte[] decrypt(byte[] ciphertext) throws Exception;

}

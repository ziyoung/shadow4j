package net.ziyoung.shadow;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;


/**
 * key derivation util
 */
public class KdUtil {

    public static byte[] computeKdf(byte[] ikm, int size) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] result = new byte[size];
        byte[] digest = new byte[0];
        int pos = 0;
        while (true) {
            md.update(digest);
            md.update(ikm);
            digest = md.digest();
            int l = digest.length;
            md.reset();
            if (pos + l < size) {
                System.arraycopy(digest, 0, result, pos, l);
                pos += digest.length;
            } else {
                System.arraycopy(digest, 0, result, pos, size - pos);
                break;
            }
        }
        return result;
    }

    public static byte[] hkdfSha1(byte[] ikm, byte[] salt, byte[] info, int size) throws Exception {
        return computeHkdf("HmacSHA1", ikm, salt, info, size);
    }

    public static byte[] hkdfSha256(byte[] ikm, byte[] salt, byte[] info, int size) throws Exception {
        return computeHkdf("HmacSHA256", ikm, salt, info, size);
    }

    // reference https://github.com/google/tink/blob/eafd9283b1d1da1dfc08c5297c101cd4b2d530c5/java_src/src/main/java/com/google/crypto/tink/subtle/Hkdf.java#L29
    private static byte[] computeHkdf(String macAlgorithm, byte[] ikm, byte[] salt, byte[] info, int size) throws Exception {
        Mac mac = Mac.getInstance(macAlgorithm);
        if (salt == null || salt.length == 0) {
            mac.init(new SecretKeySpec(new byte[mac.getMacLength()], macAlgorithm));
        } else {
            mac.init(new SecretKeySpec(salt, macAlgorithm));
        }
        byte[] prk = mac.doFinal(ikm); // pseudorandom key
        byte[] result = new byte[size];
        int ctr = 1;
        int pos = 0;
        mac.init(new SecretKeySpec(prk, macAlgorithm));
        byte[] digest = new byte[0];
        while (true) {
            mac.update(digest);
            mac.update(info);
            mac.update((byte) ctr);
            digest = mac.doFinal();
            if (pos + digest.length < size) {
                System.arraycopy(digest, 0, result, pos, digest.length);
                pos += digest.length;
                ctr++;
            } else {
                System.arraycopy(digest, 0, result, pos, size - pos);
                break;
            }
        }
        return result;
    }

}

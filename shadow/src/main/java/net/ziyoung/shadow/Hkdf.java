package net.ziyoung.shadow;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * https://github.com/google/tink/blob/eafd9283b1d1da1dfc08c5297c101cd4b2d530c5/java_src/src/main/java/com/google/crypto/tink/subtle/Hkdf.java#L29
 */

public class Hkdf {

    public static byte[] hkdfSha1(byte[] ikm, byte[] salt, byte[] info) throws Exception {
        return computeHkdf("HMACSHA1", ikm, salt, info);
    }

    public static byte[] hkdfSha256(byte[] ikm, byte[] salt, byte[] info) throws Exception {
        return computeHkdf("HmacSha256", ikm, salt, info);
    }

    // ikm: input keying material
    private static byte[] computeHkdf(String macAlgorithm, byte[] ikm, byte[] salt, byte[] info) throws Exception {
        Mac mac = Mac.getInstance(macAlgorithm);
        if (salt == null || salt.length == 0) {
            mac.init(new SecretKeySpec(new byte[mac.getMacLength()], macAlgorithm));
        } else {
            mac.init(new SecretKeySpec(salt, macAlgorithm));
        }
        int size = mac.getMacLength();
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

package net.ziyoung.shadow4j.shadow;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShadowStream {

    public static final int MAX_PAYLOAD_LENGTH = 0x3FFF;

    private final byte[] data;

    public boolean sendSalt() {
        return false;
    }

}

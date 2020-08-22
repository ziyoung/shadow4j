package net.ziyoung.shadow4j.shadow;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShadowPacket {

    public static final int MAX_PAYLOAD_LENGTH = 64 * 1024;

    private final byte[] data;

}

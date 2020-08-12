package net.ziyoung.shadow;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShadowStream {

    public static final int MAX_LENGTH = 0x3FFF;
    private final byte[] data;

}

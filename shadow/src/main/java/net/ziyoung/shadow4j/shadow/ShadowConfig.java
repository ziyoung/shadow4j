package net.ziyoung.shadow4j.shadow;

import lombok.Data;

import java.net.InetSocketAddress;

@Data
public class ShadowConfig {

    private final InetSocketAddress server;
    private final String cipherName;
    private final byte[] password;

}

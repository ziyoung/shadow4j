package net.ziyoung.shadow4j.client;

import lombok.Builder;
import lombok.Data;
import net.ziyoung.shadow4j.shadow.ShadowConfig;

import java.net.InetSocketAddress;

@Data
@Builder
public class ClientConfig {

    private final ShadowConfig shadowConfig;
    private final InetSocketAddress socks;
    private final boolean verboseMode;
    private final boolean unpAssociate;

}

package net.ziyoung.shadow4j.server;

import lombok.Builder;
import lombok.Data;
import net.ziyoung.shadow4j.shadow.ShadowConfig;

@Data
@Builder
public class ServerConfig {

    private final ShadowConfig shadowConfig;
    private final int port;
    private final boolean verboseMode;

}

package net.ziyoung.shadow4j.client;

import lombok.Builder;
import lombok.Data;
import net.ziyoung.shadow4j.shadow.ShadowConfig;

@Data
@Builder
public class ClientConfig {

    private final ShadowConfig shadowConfig;
    private final String socks;
    private final boolean verboseMode;

}

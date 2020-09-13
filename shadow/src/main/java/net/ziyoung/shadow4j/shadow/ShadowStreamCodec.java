package net.ziyoung.shadow4j.shadow;

import io.netty.channel.CombinedChannelDuplexHandler;

public class ShadowStreamCodec extends CombinedChannelDuplexHandler<ShadowStreamDecoder, ShadowStreamEncoder> {

    public ShadowStreamCodec(ShadowConfig config) {
        super(new ShadowStreamDecoder(config), new ShadowStreamEncoder(config));
    }


    public ShadowStreamCodec(ShadowConfig config, boolean isServerMode) {
        super(new ShadowStreamDecoder(config, isServerMode), new ShadowStreamEncoder(config, isServerMode));
    }

}

package net.ziyoung.shadow;

import io.netty.channel.CombinedChannelDuplexHandler;

public class CombinedShadowStreamCodec extends CombinedChannelDuplexHandler<ShadowStreamDecoder, ShadowStreamEncoder> {

    public CombinedShadowStreamCodec(ShadowConfig config) {
        super(new ShadowStreamDecoder(config), new ShadowStreamEncoder(config));
    }

}

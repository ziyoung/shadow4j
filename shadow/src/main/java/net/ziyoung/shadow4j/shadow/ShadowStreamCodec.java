package net.ziyoung.shadow4j.shadow;

import io.netty.channel.CombinedChannelDuplexHandler;

public class ShadowStreamCodec extends CombinedChannelDuplexHandler<ShadowStreamDecoder, ShadowStreamEncoder> {

    public ShadowStreamCodec(ShadowConfig config) {
        super(new ShadowStreamDecoder(config), new ShadowStreamEncoder(config));
    }


    public ShadowStreamCodec(ShadowConfig config, boolean isClientMode) {
        super(new ShadowStreamDecoder(config, isClientMode), new ShadowStreamEncoder(config));
    }

}

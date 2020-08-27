package net.ziyoung.shadow4j.shadow;

import io.netty.channel.CombinedChannelDuplexHandler;

public class CombinedShadowPacketCodec extends CombinedChannelDuplexHandler<ShadowPacketDecoder, ShadowPacketEncoder> {

    public CombinedShadowPacketCodec(ShadowConfig config) {
        super(new ShadowPacketDecoder(config), new ShadowPacketEncoder(config));
    }

}

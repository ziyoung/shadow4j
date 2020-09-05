package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.EncoderException;

import java.util.Objects;

public final class ShadowUtils {

    public static void closeChannelOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static int parseSocksOption(String option) {
        if (option.contains(":")) {
            return Integer.parseInt(option.split(":")[1]);
        }
        return Integer.parseInt(option);
    }

    public static void checkShadowStream(ShadowStream stream) {
        Objects.requireNonNull(stream);
        byte[] data = stream.getData();
        int size = data.length;
        if (size > ShadowStream.MAX_PAYLOAD_LENGTH) {
            throw new EncoderException("fail to decode stream: invalid data size " + size);
        }
    }

    public static int shorBytesToInt(byte[] bytes) {
        if (bytes == null || bytes.length != 2) {
            throw new IllegalArgumentException("invalid bytes");
        }
        return (bytes[0] << 8) + Byte.toUnsignedInt(bytes[1]);
    }

    public static byte[] intToShortBytes(int i) {
        return new byte[]{(byte) (i >> 8), (byte) i};
    }

}

package net.ziyoung.shadow4j.shadow;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.EncoderException;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public final class ShadowUtil {

    public static void closeChannelOnFlush(Channel channel) {
        if (channel.isActive()) {
            channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static ShadowConfig parseClientUrl(String url) throws Exception {
        if (!url.startsWith("ss://")) {
            url = "ss://" + url;
        }
        URI uri = new URI(url);
        String userInfo = uri.getUserInfo();
        String host = uri.getHost();
        int port = uri.getPort();
        if (userInfo == null || host == null || port == -1) {
            throw new IllegalArgumentException("invalid url");
        }

        String[] strings = userInfo.split(":");
        if (strings.length != 2) {
            throw new IllegalArgumentException("invalid authority: cipher and password is required");
        }

        String cipher = strings[0].toLowerCase();
        byte[] password = strings[1].getBytes(StandardCharsets.UTF_8);
        if (!MetaCipher.CIPHER_CONFIG.containsKey(cipher)) {
            throw new IllegalArgumentException("invalid cipher name");
        }
        if (password.length == 0) {
            throw new IllegalArgumentException("password is required");
        }
        int size = MetaCipher.CIPHER_CONFIG.get(cipher);
        byte[] key = KdUtil.computeKdf(password, size);

        return new ShadowConfig(new InetSocketAddress(host, port), strings[0], key);
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

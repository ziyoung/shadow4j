package net.ziyoung.shadow4j.shadow;

import org.apache.commons.validator.routines.InetAddressValidator;

import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ShadowAddress extends ShadowStream {

    private final static String INVALID_ADDRESS = "invalid shadow address";

    public static ShadowAddress valueOf(URI uri) throws Exception {
        return valueOf(uri.getHost(), uri.getPort());
    }

    public static ShadowAddress valueOf(String host, int port) throws Exception {
        if (port == -1) {
            throw new IllegalArgumentException("invalid url: port is -1");
        }

        byte[] data;
        byte[] address;
        byte type;
        InetAddressValidator validator = InetAddressValidator.getInstance();
        if (validator.isValid(host)) {
            address = InetAddress.getByName(host).getAddress();
            if (address.length == 4) {
                type = Type.IPv4.code;
            } else {
                type = Type.IPv6.code;
            }
            data = new byte[1 + address.length + 2];
            data[0] = type;
            System.arraycopy(address, 0, data, 1, address.length);
        } else {
            address = host.getBytes(StandardCharsets.UTF_8);
            if (address.length > 255) {
                throw new IllegalStateException("host size is greater than 255");
            }
            type = Type.DomainName.code;
            data = new byte[1 + 1 + address.length + 2];
            data[0] = type;
            data[1] = (byte) address.length;
            System.arraycopy(address, 0, data, 2, address.length);
        }

        byte[] bytes = ShadowUtils.intToShortBytes(port);
        data[data.length - 2] = bytes[0];
        data[data.length - 1] = bytes[1];

        return new ShadowAddress(data);
    }

    public ShadowAddress(byte[] data) {
        super(data);
    }

    @Override
    public String toString() {
        byte[] data = getData();
        int length = data.length;
        if (length < 3) {
            return INVALID_ADDRESS;
        }

        boolean isDomainName = data[0] == Type.DomainName.code;
        int from = isDomainName ? 2 : 1;
        int port = (data[length - 2] << 8) + Byte.toUnsignedInt(data[length - 1]);
        byte[] address = Arrays.copyOfRange(data, from, length - 2);
        if (isDomainName) {
            return new String(address) + ":" + port;
        } else {
            try {
                InetAddress inetAddress = InetAddress.getByAddress(address);
                return inetAddress.getHostAddress() + ":" + port;
            } catch (Exception exception) {
                return INVALID_ADDRESS;
            }
        }
    }

    enum Type {

        IPv4((byte) 1),
        DomainName((byte) 3),
        IPv6((byte) 4);

        private final byte code;

        Type(byte code) {
            this.code = code;
        }

    }

}

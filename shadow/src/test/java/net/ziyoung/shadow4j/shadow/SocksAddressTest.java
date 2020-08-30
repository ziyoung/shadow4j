package net.ziyoung.shadow4j.shadow;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

@Slf4j
public class SocksAddressTest {

    @Test
    @DisplayName("parse address")
    void testParseAddress() {
        String[] inputs = new String[]{
                "baidu.com:80",
                "example.com:1234",
                "220.181.38.148:80"
        };
        // results are derived from https://github.com/ziyoung/socks5-dump/blob/041105d4000956c0ea8c706291557de04ff927fb/main_test.go#L17
        String[] results = new String[]{
                "030962616964752e636f6d0050",
                "030b6578616d706c652e636f6d04d2",
                "01dcb526940050"
        };

        for (int i = 0; i < inputs.length; i++) {
            String input = inputs[i];
            log.debug("current input is {}", input);
            URI uri = Assertions.assertDoesNotThrow(() -> new URI("ss://" + input));
            SocksAddress address = Assertions.assertDoesNotThrow(() -> SocksAddress.valueOf(uri));
            Assertions.assertEquals(input, address.toString());
            Assertions.assertEquals(results[i], Hex.encodeHexString(address.getData()));
        }
    }

}

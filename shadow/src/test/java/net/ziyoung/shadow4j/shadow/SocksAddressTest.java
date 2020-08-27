package net.ziyoung.shadow4j.shadow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

public class SocksAddressTest {

    @Test
    @DisplayName("parse address")
    void parseAddress() {
        String[] inputs = new String[]{
                "example.com:1234",
                "220.181.38.148:80"
        };

        for (String input : inputs) {
            URI uri = Assertions.assertDoesNotThrow(() -> new URI("ss://" + input));
            SocksAddress address = Assertions.assertDoesNotThrow(() -> SocksAddress.valueOf(uri));
            Assertions.assertEquals(input, address.toString());
        }

    }

}

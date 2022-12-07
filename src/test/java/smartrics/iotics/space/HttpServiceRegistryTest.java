package smartrics.iotics.space;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpServiceRegistryTest {

    @Test
    public void manufacturesIndexJsonURL() {
        HttpServiceRegistry sr = new HttpServiceRegistry("dns");
        assertEquals("https://dns/index.json", sr.registryUrl());
    }
}
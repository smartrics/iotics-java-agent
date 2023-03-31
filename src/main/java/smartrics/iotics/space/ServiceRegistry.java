package smartrics.iotics.space;

import java.io.IOException;

public interface ServiceRegistry {
    HostEndpoints find() throws IOException;
}

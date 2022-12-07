package smartrics.iotics.space;

import java.io.IOException;

public interface ServiceRegistry {
    Endpoints find() throws IOException;
}

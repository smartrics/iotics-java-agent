package smartrics.iotics.space.connector;

import com.iotics.api.FeedID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.grpc.IoticsApi;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractConnector {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConnector.class);

    protected final Jsonifier jsonifier;
    protected final PrefixGenerator prefixGenerator;
    private final IoticsApi api;

    public AbstractConnector(IoticsApi api) {
        this.prefixGenerator = new PrefixGenerator();
        this.jsonifier = new Jsonifier(prefixGenerator);
        this.api = api;

    }

    protected IoticsApi getApi() {
        return this.api;
    }

    private static String IndexNameForFeed(String prefix, FeedID feedID) {
        return String.join("_", prefix, feedID.getId()).toLowerCase(Locale.ROOT);
    }

    public CompletableFuture<Void> stop(Duration timeout) {
        CompletableFuture<Void> c = new CompletableFuture<>();
        c.thenAccept(unused -> {
            api.stop(timeout);
        }).complete(null);
        return c;
    }

    public abstract CompletableFuture<Void> start();

}

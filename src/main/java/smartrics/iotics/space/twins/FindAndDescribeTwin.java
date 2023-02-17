package smartrics.iotics.space.twins;

import com.iotics.api.DescribeTwinResponse;
import com.iotics.api.SearchRequest;
import com.iotics.api.SearchResponse;
import com.iotics.api.TwinID;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.grpc.AbstractLoggingStreamObserver;
import smartrics.iotics.space.grpc.IoticsApi;

import java.time.Duration;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FindAndDescribeTwin extends FindAndDoTwin implements Describer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FindAndDescribeTwin.class);
    private final Timer schedulerTimer;

    public FindAndDescribeTwin(IoticsApi api,
                               String keyName,
                               String label,
                               Executor executor,
                               TwinID modelDid,
                               Timer shareDataTimer,
                               Timer describeSchedulerTimer,
                               Duration shareEvery) {
        super(api, keyName, label, executor, modelDid, shareDataTimer, shareEvery);
        this.schedulerTimer = describeSchedulerTimer;
    }

    public CompletableFuture<Void> describeAll(SearchRequest.Payload searchRequestPayload,
                                               Duration describeInitialDelay,
                                               Duration describeFrequency,
                                               StreamObserver<DescribeTwinResponse> twinStreamObserver) {

        CompletableFuture<Void> resFuture = new CompletableFuture<>();

        StreamObserver<SearchResponse.TwinDetails> resultsStreamObserver = new AbstractLoggingStreamObserver<>(FindAndDescribeTwin.class.getName()) {
            @Override
            public void onNext(SearchResponse.TwinDetails value) {
                try {
                    FindAndDescribeTwin.super.describe(value.getTwinId(), schedulerTimer, describeInitialDelay, describeFrequency,twinStreamObserver);
                } catch (Throwable t) {
                    LOGGER.warn("exception when describing twin {}", value, t);
                }
            }
        };
        this.search(searchRequestPayload, resultsStreamObserver);
        return resFuture;
    }
}
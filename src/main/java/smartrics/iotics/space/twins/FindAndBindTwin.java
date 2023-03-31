package smartrics.iotics.space.twins;

import com.iotics.api.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.grpc.FeedDataBag;
import smartrics.iotics.space.grpc.IoticsApi;
import smartrics.iotics.space.grpc.NoopStreamObserver;
import smartrics.iotics.space.grpc.TwinDataBag;

import java.time.Duration;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

public class FindAndBindTwin extends FindAndDoTwin implements Follower {
    private static final Logger LOGGER = LoggerFactory.getLogger(FindAndBindTwin.class);

    private final Map<FeedID, CompletableFuture<Void>> followFutures;

    private final AtomicLong feedsFollowed = new AtomicLong(0);
    private final AtomicLong datapointReceived = new AtomicLong(0);
    protected final RetryConf retryConf;

    public FindAndBindTwin(IoticsApi api,
                           String keyName,
                           String label,
                           Executor executor,
                           TwinID modelDid,
                           Timer shareDataTimer,
                           Duration shareEvery,
                           RetryConf retryConf) {
        super(api, keyName, label, executor, modelDid, shareDataTimer, shareEvery);
        this.followFutures = new ConcurrentHashMap<>();
        this.retryConf = retryConf;
    }

    protected Map<String, Object> getShareStatus() {
        Map<String, Object> data = super.getShareStatus();
        data.put(FOLLOWING_FEEDS, feedsFollowed.get());
        data.put(RECEIVED_DATA_POINTS, datapointReceived.get());
        return data;
    }

    public CompletableFuture<Void> findAndBind(SearchRequest.Payload searchRequestPayload, StreamObserver<FeedDataBag> streamObserver) {
        return this.findAndBind(searchRequestPayload, new NoopStreamObserver<>(), streamObserver);
    }

    public CompletableFuture<Void> findAndBind(SearchRequest.Payload searchRequestPayload, StreamObserver<TwinDataBag> twinStreamObserver, StreamObserver<FeedDataBag> feedDataStreamObserver) {
        try {
            this.updateMeta(searchRequestPayload);
        } catch (Exception e) {
            errorsCount.incrementAndGet();
            twinStreamObserver.onError(e);
            CompletableFuture<Void> c = new CompletableFuture<>();
            c.complete(null);
            return c;
        }

        CompletableFuture<Void> resFuture = new CompletableFuture<>();
        StreamObserver<SearchResponse.TwinDetails> resultsStreamObserver = new StreamObserver<>() {
            @Override
            public void onNext(SearchResponse.TwinDetails twinDetails) {
                try {
                    TwinDataBag twinData = TwinDataBag.from(twinDetails);
                    twinStreamObserver.onNext(twinData);
                    FindAndBindTwin.this.twinsFound.incrementAndGet();
                    FindAndBindTwin.this.lastUpdateMs.set(System.currentTimeMillis());
                    for (SearchResponse.FeedDetails feedDetails : twinDetails.getFeedsList()) {
                        FeedID feedID = feedDetails.getFeedId();
                        LOGGER.debug("[{}][{}] about to follow {}/{} in host {}", keyName, label,
                                feedID.getTwinId(), feedID.getId(), feedID.getHostId());
                        follow(feedID, FindAndBindTwin.this.retryConf, new StreamObserver<>() {
                            @Override
                            public void onNext(FetchInterestResponse value) {
                                try {
                                    FindAndBindTwin.this.datapointReceived.incrementAndGet();
                                    FindAndBindTwin.this.lastUpdateMs.set(System.currentTimeMillis());
                                    feedDataStreamObserver.onNext(new FeedDataBag(twinData, feedDetails, value));
                                } catch (RuntimeException e) {
                                    LOGGER.debug("[{}][{}] exception processing next feed data", keyName, label, e);
                                    throw e;
                                }
                            }

                            @Override
                            public void onError(Throwable t) {
                                FindAndBindTwin.this.errorsCount.incrementAndGet();
                                FindAndBindTwin.this.feedsFollowed.decrementAndGet();
                                FindAndBindTwin.this.lastUpdateMs.set(System.currentTimeMillis());
                                feedDataStreamObserver.onError(t);
                            }

                            @Override
                            public void onCompleted() {
                                FindAndBindTwin.this.feedsFollowed.decrementAndGet();
                                FindAndBindTwin.this.lastUpdateMs.set(System.currentTimeMillis());
                                feedDataStreamObserver.onCompleted();
                            }
                        });
                        FindAndBindTwin.this.feedsFollowed.incrementAndGet();
                        FindAndBindTwin.this.lastUpdateMs.set(System.currentTimeMillis());
                        followFutures.put(feedDetails.getFeedId(), new CompletableFuture<>());
                    }

                } catch (RuntimeException e) {
                    LOGGER.debug("[{}][{}] exception processing next search response", keyName, label, e);
                    throw e;
                }
            }

            @Override
            public void onError(Throwable throwable) {
                FindAndBindTwin.this.errorsCount.incrementAndGet();
                twinStreamObserver.onError(throwable);
            }

            @Override
            public void onCompleted() {
                followFutures.values().forEach(future -> future.cancel(true));
                resFuture.complete(null);
                followFutures.clear();
            }
        };
        this.search(searchRequestPayload, resultsStreamObserver);
        return resFuture;
    }
}

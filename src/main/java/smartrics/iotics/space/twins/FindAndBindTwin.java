package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.iotics.api.*;
import com.iotics.sdk.identity.SimpleIdentityManager;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.Builders;
import smartrics.iotics.space.grpc.FeedDatabag;
import smartrics.iotics.space.grpc.NoopStreamObserver;
import smartrics.iotics.space.grpc.TwinDatabag;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static smartrics.iotics.space.UriConstants.*;
import static smartrics.iotics.space.grpc.ListenableFutureAdapter.toCompletable;

public class FindAndBindTwin extends AbstractTwinWithModel implements Follower, Publisher, Searcher {

    public static final String COUNTERS_FEED_ID = "counters";
    private static final Logger LOGGER = LoggerFactory.getLogger(FindAndBindTwin.class);

    public static final String RECEIVED_DATA_POINTS = "receivedDataPoints";
    public static final String FOLLOWING_FEEDS = "followingFeeds";
    public static final String FOUND_TWINS = "foundTwins";
    public static final String ERRORS_COUNT = "errorsCount";
    public static final String TIMESTAMP = "timestamp";

    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX");
    private final FeedAPIGrpc.FeedAPIFutureStub feedStub;
    private final InterestAPIGrpc.InterestAPIStub interestStub;
    private final SearchAPIGrpc.SearchAPIStub searchStub;
    private final MetaAPIGrpc.MetaAPIStub metaStub;
    private final InterestAPIGrpc.InterestAPIBlockingStub interestBlockingStub;
    private final Map<FeedID, CompletableFuture<Void>> followFutures;

    private final AtomicLong twinsFound = new AtomicLong(0);
    private final AtomicLong errorsCount = new AtomicLong(0);
    private final AtomicLong feedsFollowed = new AtomicLong(0);
    private final AtomicLong datapointReceived = new AtomicLong(0);
    private final Gson gson;

    private final AtomicLong lastUpdateMs = new AtomicLong(-1);
    private final long shareEveryMs;
    private final RetryConf retryConf;

    public FindAndBindTwin(SimpleIdentityManager sim,
                           String keyName,
                           TwinAPIGrpc.TwinAPIFutureStub twinStub,
                           FeedAPIGrpc.FeedAPIFutureStub feedStub,
                           InterestAPIGrpc.InterestAPIStub interestStub,
                           InterestAPIGrpc.InterestAPIBlockingStub interestBlockingStub,
                           SearchAPIGrpc.SearchAPIStub searchStub,
                           MetaAPIGrpc.MetaAPIStub metaStub,
                           Executor executor,
                           TwinID modelDid,
                           Timer shareDataTimer,
                           Duration shareEvery,
                           RetryConf retryConf) {
        super(sim, keyName, twinStub, executor, modelDid);
        this.feedStub = feedStub;
        this.interestStub = interestStub;
        this.searchStub = searchStub;
        this.metaStub = metaStub;
        this.interestBlockingStub = interestBlockingStub;
        this.followFutures = new ConcurrentHashMap<>();
        this.shareEveryMs = shareEvery.getSeconds() * 1000;
        this.gson = new Gson();
        this.retryConf = retryConf;
        shareDataTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    shareStatus();
                } catch (Exception e) {
                    LOGGER.warn("exception when sharing", e);
                }
            }
        }, 0, shareEveryMs);

    }

    public void shareStatus() {
        if (!shouldShare()) return;
        Map<String, Object> data = new HashMap<>();
        data.put(FOLLOWING_FEEDS, feedsFollowed.get());
        data.put(RECEIVED_DATA_POINTS, datapointReceived.get());
        data.put(FOUND_TWINS, twinsFound.get());
        data.put(ERRORS_COUNT, errorsCount.get());
        data.put(TIMESTAMP, LocalDateTime.now().atOffset(ZoneOffset.UTC).format(dtf));

        LOGGER.info("sharing counters data: {}", data);
        toCompletable(feedStub.shareFeedData(ShareFeedDataRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setArgs(ShareFeedDataRequest.Arguments.newBuilder()
                        .setFeedId(FeedID.newBuilder()
                                .setTwinId(this.getIdentity().did())
                                .setId(COUNTERS_FEED_ID).build())
                        .build())
                .setPayload(ShareFeedDataRequest.Payload.newBuilder()
                        .setSample(FeedData.newBuilder()
                                .setMime("application/json")
                                .setData(ByteString.copyFrom(gson.toJson(data), Charset.defaultCharset()))
                                .build())
                        .build())
                .build()))
                .thenAccept(shareFeedDataResponse -> {
                    LOGGER.info("shared counters data: {}", data);
                });
    }

    private boolean shouldShare() {
        // no need to share since nothing has updated yet
        return System.currentTimeMillis() - this.shareEveryMs <= this.lastUpdateMs.get();
    }

    public void updateMeta(String content, String type) {
        toCompletable(getTwinAPIFutureStub().updateTwin(UpdateTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setArgs(UpdateTwinRequest.Arguments.newBuilder()
                        .setTwinId(TwinID.newBuilder().setId(getIdentity().did()).build())
                        .build())
                .setPayload(UpdateTwinRequest.Payload.newBuilder()
                        .setProperties(PropertyUpdate.newBuilder()
                                .addDeletedByKey(IOTICS_CUSTOM_SEARCH_VALUE_PROP)
                                .addDeletedByKey(IOTICS_CUSTOM_SEARCH_TYPE_PROP)
                                .addAdded(Property.newBuilder()
                                        .setKey(IOTICS_CUSTOM_SEARCH_VALUE_PROP)
                                        .setStringLiteralValue(StringLiteral.newBuilder()
                                                .setValue(content)
                                                .build())
                                        .build())
                                .addAdded(Property.newBuilder()
                                        .setKey(IOTICS_CUSTOM_SEARCH_TYPE_PROP)
                                        .setStringLiteralValue(StringLiteral.newBuilder()
                                                .setValue(type)
                                                .build())
                                        .build())
                                .build())
                        .build())
                .build()))
                .thenAccept(upsertTwinResponse -> LOGGER.info("Update complete to store metadata with {}", content));
    }

    public void updateMeta(SearchRequest.Payload searchRequestPayload) throws InvalidProtocolBufferException {
        String content = JsonFormat.printer()
                .omittingInsignificantWhitespace()
                .preservingProtoFieldNames()
                .sortingMapKeys()
                .print(SearchRequest.Payload.newBuilder(searchRequestPayload));
        String type = "simple";

        updateMeta(content, type);
    }

    @Override
    public ListenableFuture<UpsertTwinResponse> make() {
        return getTwinAPIFutureStub().upsertTwin(UpsertTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setPayload(UpsertTwinRequest.Payload.newBuilder()
                        .setTwinId(TwinID.newBuilder().setId(getIdentity().did()).build())
                        .setVisibility(Visibility.PRIVATE)
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDFS_COMMENT_PROP)
                                .setLiteralValue(Literal.newBuilder()
                                        .setValue("FindAndBindTwin: it follows feeds and makes them available for post processing")
                                        .build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDFS_LABEL_PROP)
                                .setLiteralValue(Literal.newBuilder().setValue("FindAndBindTwin").build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(IOTICS_APP_MODEL_PROP)
                                .setUriValue(Uri.newBuilder().setValue(getModelDid().getId()).build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDF_TYPE_PROP)
                                .setUriValue(Uri.newBuilder().setValue("https://data.iotics.com/ont/receiver").build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(IOTICS_PUBLIC_ALLOW_LIST_PROP)
                                .setUriValue(Uri.newBuilder().setValue(IOTICS_PUBLIC_ALLOW_ALL_VALUE).build())
                                .build())
                        .addFeeds(UpsertFeedWithMeta.newBuilder()
                                .setId(COUNTERS_FEED_ID)
                                .setStoreLast(true)
                                .addProperties(Property.newBuilder()
                                        .setKey(ON_RDFS_COMMENT_PROP)
                                        .setLiteralValue(Literal.newBuilder().setValue("following data counters").build())
                                        .build())
                                .addProperties(Property.newBuilder()
                                        .setKey(ON_RDFS_LABEL_PROP)
                                        .setLiteralValue(Literal.newBuilder().setValue("Counters").build())
                                        .build())
                                .addValues(Value.newBuilder()
                                        .setLabel(RECEIVED_DATA_POINTS).setComment("count data points received since start of the connector")
                                        .setDataType("integer")
                                        .build())
                                .addValues(Value.newBuilder()
                                        .setLabel(FOLLOWING_FEEDS).setComment("number of feeds actively followed")
                                        .setDataType("integer")
                                        .build())
                                .addValues(Value.newBuilder()
                                        .setLabel(FOUND_TWINS).setComment("number of feeds actively followed")
                                        .setDataType("integer")
                                        .build())
                                .addValues(Value.newBuilder()
                                        .setLabel(TIMESTAMP).setComment("update date")
                                        .setDataType("dateTime")
                                        .build())
                                .build())
                        .build())
                .build());
    }

    @Override
    public InterestAPIGrpc.InterestAPIStub getInterestAPIStub() {
        return this.interestStub;
    }

    @Override
    public InterestAPIGrpc.InterestAPIBlockingStub getInterestAPIBlockingStub() {
        return this.interestBlockingStub;
    }

    @Override
    public FeedAPIGrpc.FeedAPIFutureStub getFeedAPIFutureStub() {
        return this.feedStub;
    }

    @Override
    public SearchAPIGrpc.SearchAPIStub getSearchAPIStub() {
        return this.searchStub;
    }

    @Override
    public MetaAPIGrpc.MetaAPIStub getMetaAPIStub() {
        return this.metaStub;
    }

    public CompletableFuture<Void> findAndBind(SearchRequest.Payload searchRequestPayload, StreamObserver<FeedDatabag> streamObserver) {
        return this.findAndBind(searchRequestPayload, new NoopStreamObserver<TwinDatabag>(), streamObserver);
    }

    public CompletableFuture<Void> findAndBind(SearchRequest.Payload searchRequestPayload, StreamObserver<TwinDatabag> twinStreamObserver, StreamObserver<FeedDatabag> feedDataStreamObserver) {
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
                    TwinDatabag twinData = new TwinDatabag(twinDetails);
                    twinStreamObserver.onNext(twinData);
                    FindAndBindTwin.this.twinsFound.incrementAndGet();
                    FindAndBindTwin.this.lastUpdateMs.set(System.currentTimeMillis());
                    for (SearchResponse.FeedDetails feedDetails : twinDetails.getFeedsList()) {
                        FeedID feedID = feedDetails.getFeedId();
                        LOGGER.debug("about to follow {}/{} in host {}", feedID.getTwinId(), feedID.getId(), feedID.getHostId());
                        follow(feedID, FindAndBindTwin.this.retryConf, new StreamObserver<>() {
                            @Override
                            public void onNext(FetchInterestResponse value) {
                                try {
                                    FindAndBindTwin.this.datapointReceived.incrementAndGet();
                                    FindAndBindTwin.this.lastUpdateMs.set(System.currentTimeMillis());
                                    feedDataStreamObserver.onNext(new FeedDatabag(twinData, feedDetails, value));
                                } catch (RuntimeException e) {
                                    LOGGER.debug("exception processing next feed data", e);
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
                    LOGGER.debug("exception processing next search response", e);
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

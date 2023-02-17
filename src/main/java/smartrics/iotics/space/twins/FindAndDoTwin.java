package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.iotics.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import smartrics.iotics.space.Builders;
import smartrics.iotics.space.grpc.IoticsApi;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

import static smartrics.iotics.space.UriConstants.*;
import static smartrics.iotics.space.grpc.ListenableFutureAdapter.toCompletable;

public abstract class FindAndDoTwin extends AbstractTwinWithModel implements Publisher, Searcher {

    public static final String COUNTERS_FEED_ID = "counters";
    public static final String RECEIVED_DATA_POINTS = "receivedDataPoints";
    public static final String FOLLOWING_FEEDS = "followingFeeds";
    public static final String FOUND_TWINS = "foundTwins";
    public static final String ERRORS_COUNT = "errorsCount";
    public static final String TIMESTAMP = "timestamp";
    protected static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
    private static final Logger LOGGER = LoggerFactory.getLogger(FindAndDoTwin.class);
    protected final AtomicLong twinsFound = new AtomicLong(0);
    protected final AtomicLong errorsCount = new AtomicLong(0);
    protected final String keyName;
    final AtomicLong lastUpdateMs = new AtomicLong(-1);
    final String label;
    private final Gson gson;
    private final long shareEveryMs;

    public FindAndDoTwin(IoticsApi api,
                         String keyName,
                         String label,
                         Executor executor,
                         TwinID modelDid,
                         Timer shareDataTimer,
                         Duration shareEvery) {
        super(api, keyName, executor, modelDid);
        this.shareEveryMs = shareEvery.getSeconds() * 1000;
        this.gson = new Gson();
        this.label = label;
        this.keyName = keyName;
        shareDataTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    shareStatus();
                } catch (Exception e) {
                    LOGGER.warn("[{}][{}] exception when sharing", keyName, label, e);
                }
            }
        }, 0, shareEveryMs);

    }

    protected Map<String, Object> getShareStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put(FOUND_TWINS, twinsFound.get());
        data.put(ERRORS_COUNT, errorsCount.get());
        data.put(TIMESTAMP, LocalDateTime.now().atOffset(ZoneOffset.UTC).format(dtf));
        return data;
    }

    public void shareStatus() {
        if (!shouldShare()) return;
        Map<String, Object> data = getShareStatus();

        LOGGER.info("[{}][{}] sharing counters data: {}", this.keyName, this.label, data);
        toCompletable(ioticsApi().feedAPIFutureStub().shareFeedData(ShareFeedDataRequest.newBuilder()
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
                .thenAccept(shareFeedDataResponse -> LOGGER.info("[{}][{}] shared counters data: {}", keyName, label, data));
    }

    private boolean shouldShare() {
        // no need to share since nothing has updated yet
        return System.currentTimeMillis() - this.shareEveryMs <= this.lastUpdateMs.get();
    }

    public void updateMeta(String content, String type) {
        toCompletable(ioticsApi().twinAPIFutureStub().updateTwin(UpdateTwinRequest.newBuilder()
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
                .thenAccept(upsertTwinResponse ->
                        LOGGER.info("[{}][{}] Update complete to store metadata with {}", keyName, label, content));
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
        String name = this.getClass().getName();
        return ioticsApi().twinAPIFutureStub().upsertTwin(UpsertTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setPayload(UpsertTwinRequest.Payload.newBuilder()
                        .setTwinId(TwinID.newBuilder().setId(getIdentity().did()).build())
                        .setVisibility(Visibility.PRIVATE)
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDFS_COMMENT_PROP)
                                .setLiteralValue(Literal.newBuilder()
                                        .setValue(name + " [" + this.keyName + "][" +
                                                this.label + "]: it follows feeds and makes them available for post processing")
                                        .build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDFS_LABEL_PROP)
                                .setLiteralValue(Literal.newBuilder().setValue(name + " [" +
                                        this.keyName + "][" + this.label + "]").build())
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
                                        .setLiteralValue(Literal.newBuilder().setValue("Statistics for this twin").build())
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
}
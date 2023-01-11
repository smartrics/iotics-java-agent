package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.iotics.api.*;
import com.iotics.sdk.identity.SimpleIdentityManager;
import io.grpc.stub.StreamObserver;
import smartrics.iotics.space.Builders;
import smartrics.iotics.space.grpc.AbstractLoggingStreamObserver;
import smartrics.iotics.space.grpc.NoopStreamObserver;
import smartrics.iotics.space.grpc.TwinData;
import smartrics.iotics.space.grpc.FeedData;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static smartrics.iotics.space.UriConstants.*;

public class FindAndBindTwin extends AbstractTwinWithModel implements Follower, Publisher, Searcher {
    private final FeedAPIGrpc.FeedAPIFutureStub feedStub;
    private final InterestAPIGrpc.InterestAPIStub interestStub;
    private final SearchAPIGrpc.SearchAPIStub searchStub;
    private final InterestAPIGrpc.InterestAPIBlockingStub interestBlockingStub;

    public FindAndBindTwin(SimpleIdentityManager sim,
                           String keyName,
                           TwinAPIGrpc.TwinAPIFutureStub twinStub,
                           FeedAPIGrpc.FeedAPIFutureStub feedStub,
                           InterestAPIGrpc.InterestAPIStub interestStub,
                           InterestAPIGrpc.InterestAPIBlockingStub interestBlockingStub,
                           SearchAPIGrpc.SearchAPIStub searchStub,
                           Executor executor,
                           TwinID modelDid) {
        super(sim, keyName, twinStub, executor, modelDid);
        this.feedStub = feedStub;
        this.interestStub = interestStub;
        this.searchStub = searchStub;
        this.interestBlockingStub = interestBlockingStub;
    }

    @Override
    public ListenableFuture<UpsertTwinResponse> make() {
        return getTwinAPIFutureStub().upsertTwin(UpsertTwinRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setPayload(UpsertTwinRequest.Payload.newBuilder()
                        .setTwinId(TwinID.newBuilder().setId(getIdentity().did()).build())
                        .setVisibility(Visibility.PRIVATE)
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDFS_COMMENT)
                                .setLiteralValue(Literal.newBuilder().setValue("Data receiver: it follows feeds and makes them available for post processing").build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDFS_LABEL)
                                .setLiteralValue(Literal.newBuilder().setValue("DataReceive").build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(IOTICS_APP_MODEL)
                                .setUriValue(Uri.newBuilder().setValue(getModelDid().getId()).build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(ON_RDF_TYPE)
                                .setUriValue(Uri.newBuilder().setValue("https://data.iotics.com/ont/receiver").build())
                                .build())
                        .addProperties(Property.newBuilder()
                                .setKey(IOTICS_PUBLIC_ALLOW_LIST)
                                .setUriValue(Uri.newBuilder().setValue(IOTICS_PUBLIC_ALLOW_ALL).build())
                                .build())
                        .addFeeds(UpsertFeedWithMeta.newBuilder()
                                .setId("status")
                                .setStoreLast(true)
                                .addValues(Value.newBuilder()
                                        .setLabel("status").setComment("twin status")
                                        .setDataType("boolean")
                                        .build())
                                .addProperties(Property.newBuilder()
                                        .setKey(ON_RDFS_COMMENT)
                                        .setLiteralValue(Literal.newBuilder().setValue("Twin status").build())
                                        .build())
                                .addProperties(Property.newBuilder()
                                        .setKey(ON_RDFS_LABEL)
                                        .setLiteralValue(Literal.newBuilder().setValue("Status").build())
                                        .build())
                                .addValues(Value.newBuilder()
                                        .setLabel("message").setComment("twin status message")
                                        .setDataType("string")
                                        .build())
                                .addValues(Value.newBuilder()
                                        .setLabel("count").setComment("count data points received since start of the connector")
                                        .setDataType("integer")
                                        .build())
                                .addValues(Value.newBuilder()
                                        .setLabel("timestamp").setComment("update date")
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

    public CompletableFuture<Void> findAndBind(SearchFilter searchFilter, StreamObserver<FeedData> streamObserver) {
        return this.findAndBind(searchFilter, new NoopStreamObserver<TwinData>(), streamObserver);
    }

    public CompletableFuture<Void> findAndBind(SearchFilter searchFilter, StreamObserver<TwinData> twinStreamObserver, StreamObserver<FeedData> feedDataStreamObserver) {
        CompletableFuture<Void> resFuture = new CompletableFuture<>();
        StreamObserver<SearchResponse.TwinDetails> resultsStreamObserver = new AbstractLoggingStreamObserver<>("'search'") {
            @Override
            public void onNext(SearchResponse.TwinDetails twinDetails) {
                TwinData twinData = new TwinData(twinDetails);
                twinStreamObserver.onNext(twinData);
                for (SearchResponse.FeedDetails feedDetails : twinDetails.getFeedsList()) {
                    follow(feedDetails.getFeedId(), new AbstractLoggingStreamObserver<>(feedDetails.getFeedId().toString()) {
                        @Override
                        public void onNext(FetchInterestResponse value) {
                            feedDataStreamObserver.onNext(new FeedData(twinData, feedDetails, value));
                        }
                    });
                }
            }

            @Override
            public void onCompleted() {
                super.onCompleted();
                resFuture.complete(null);
            }
        };
        this.search(searchFilter, resultsStreamObserver);
        return resFuture;
    }

}

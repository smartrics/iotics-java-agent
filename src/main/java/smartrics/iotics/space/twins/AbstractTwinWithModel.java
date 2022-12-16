package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.iotics.api.*;
import com.iotics.sdk.identity.SimpleIdentityManager;
import smartrics.iotics.space.Builders;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

public abstract class AbstractTwinWithModel extends AbstractTwin {

    protected final TwinID modelDid;
    protected final FeedAPIGrpc.FeedAPIFutureStub feedStub;

    public AbstractTwinWithModel(SimpleIdentityManager sim, String keyName,
                                 TwinAPIGrpc.TwinAPIFutureStub stub,
                                 FeedAPIGrpc.FeedAPIFutureStub feedStub,
                                 Executor executor, TwinID modelDid) {
        super(sim, keyName, stub, executor);
        this.modelDid = modelDid;
        this.feedStub = feedStub;
    }

    protected ListenableFuture<ShareFeedDataResponse> share(FeedID feedID, String payload) {
        return this.feedStub.shareFeedData(ShareFeedDataRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(sim.agentIdentity().did())
                        .build())
                .setPayload(ShareFeedDataRequest.Payload.newBuilder()
                        .setSample(FeedData.newBuilder()
                                .setData(ByteString.copyFrom(payload.getBytes(StandardCharsets.UTF_8)))
                                .build())
                        .build())
                .setArgs(ShareFeedDataRequest.Arguments.newBuilder()
                        .setFeedId(feedID)
                        .build())
                .build());
    }
}

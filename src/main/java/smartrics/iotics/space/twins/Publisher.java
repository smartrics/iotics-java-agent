package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.iotics.api.*;
import smartrics.iotics.space.Builders;

import java.nio.charset.StandardCharsets;

public interface Publisher extends Identifiable {

    FeedAPIGrpc.FeedAPIFutureStub getFeedAPIFutureStub();

    default ListenableFuture<ShareFeedDataResponse> share(FeedID feedID, String payload) {
        return getFeedAPIFutureStub().shareFeedData(ShareFeedDataRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
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

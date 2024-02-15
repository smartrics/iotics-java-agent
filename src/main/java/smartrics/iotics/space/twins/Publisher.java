package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.iotics.api.FeedData;
import com.iotics.api.FeedID;
import com.iotics.api.ShareFeedDataRequest;
import com.iotics.api.ShareFeedDataResponse;
import io.grpc.stub.StreamObserver;
import org.jetbrains.annotations.NotNull;
import smartrics.iotics.space.Builders;

import java.nio.charset.StandardCharsets;

public interface Publisher extends Identifiable, ApiUser {

    default ListenableFuture<ShareFeedDataResponse> share(ShareFeedDataRequest request) {
        return ioticsApi().feedAPIFutureStub().shareFeedData(request);
    }
    default StreamObserver<ShareFeedDataRequest> stream(StreamObserver<ShareFeedDataResponse> response) {
        return ioticsApi().feedAPIStub().streamFeedData(response);
    }

    default ListenableFuture<ShareFeedDataResponse> share(FeedID feedID, String payload) {
        ShareFeedDataRequest request = newRequest(feedID, payload);
        return share(request);
    }

    @NotNull
    private ShareFeedDataRequest newRequest(FeedID feedID, String payload) {
        return ShareFeedDataRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setPayload(ShareFeedDataRequest.Payload.newBuilder()
                        .setSample(FeedData.newBuilder()
                                .setData(ByteString.copyFrom(payload.getBytes(StandardCharsets.UTF_8)))
                                .build())
                        .build())
                .setArgs(ShareFeedDataRequest.Arguments.newBuilder()
                        .setFeedId(feedID)
                        .build())
                .build();
    }
}

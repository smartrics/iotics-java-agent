package smartrics.iotics.space.twins;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import com.iotics.api.FeedData;
import com.iotics.api.FeedID;
import com.iotics.api.ShareFeedDataRequest;
import com.iotics.api.ShareFeedDataResponse;
import smartrics.iotics.space.Builders;

import java.nio.charset.StandardCharsets;

public interface Publisher extends Identifiable, ApiUser {

    default ListenableFuture<ShareFeedDataResponse> share(FeedID feedID, String payload) {
        return ioticsApi().feedAPIFutureStub().shareFeedData(ShareFeedDataRequest.newBuilder()
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

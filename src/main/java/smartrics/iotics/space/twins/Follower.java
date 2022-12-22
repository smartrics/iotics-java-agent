package smartrics.iotics.space.twins;

import com.google.protobuf.BoolValue;
import com.iotics.api.*;
import io.grpc.stub.StreamObserver;
import smartrics.iotics.space.Builders;

public interface Follower extends Identifiable {

    InterestAPIGrpc.InterestAPIStub getInterestAPIStub();

    default void follow(FeedID feedId, StreamObserver<FetchInterestResponse> responseStreamObserver)  {
        FetchInterestRequest request = FetchInterestRequest.newBuilder()
                .setHeaders(Builders.newHeadersBuilder(getAgentIdentity().did()).build())
                .setFetchLastStored(BoolValue.newBuilder().setValue(true).build())
                .setArgs(FetchInterestRequest.Arguments.newBuilder()
                        .setInterest(Interest.newBuilder()
                                .setFollowerTwinId(TwinID.newBuilder().setId(getIdentity().did()))
                                .setFollowedFeedId(feedId)
                                .build())
                        .build())
                .build();
        getInterestAPIStub().fetchInterests(request, responseStreamObserver);
    }

}
